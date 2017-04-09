package controllers;

import akka.japi.Pair;
import com.example.auction.bidding.api.*;
import com.example.auction.item.api.*;
import com.example.auction.user.api.User;
import com.example.auction.user.api.UserService;
import com.lightbend.lagom.javadsl.api.transport.TransportException;
import org.pcollections.PSequence;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Http;
import play.mvc.Result;
import views.html.editItem;

import play.Configuration;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.example.auction.security.ClientSecurity.*;

public class ItemController extends AbstractController {

    private final FormFactory formFactory;
    private final ItemService itemService;
    private final BiddingService bidService;

    private Configuration config;
    private Boolean showInlineInstruction;

    @Inject
    public ItemController(Configuration config, MessagesApi messagesApi, UserService userService, FormFactory formFactory,
                          ItemService itemService, BiddingService bidService) {
        super(messagesApi, userService);
        this.formFactory = formFactory;
        this.itemService = itemService;
        this.bidService = bidService;

        this.config = config;
        showInlineInstruction = config.getBoolean("play.instruction.show");
    }

    public CompletionStage<Result> createItemForm() {
        return requireUser(ctx(), user ->
                loadNav(user).thenApply(nav ->
                        ok(views.html.createItem.render(showInlineInstruction, formFactory.form(ItemForm.class).fill(new ItemForm()), nav))
                )
        );
    }

    public CompletionStage<Result> createItem() {
        Http.Context ctx = ctx();
        return requireUser(ctx, user -> {

            Form<ItemForm> form = formFactory.form(ItemForm.class).bindFromRequest(ctx.request());

            if (form.hasErrors()) {
                return loadNav(user).thenApply(nav ->
                        ok(views.html.createItem.render(showInlineInstruction, form, nav))
                );
            } else {
                ItemData payload = fromForm(form.get());
                return itemService
                        .createItem()
                        .handleRequestHeader(authenticate(user))
                        .invoke(payload)
                        .thenApply(
                                item -> redirect(routes.ItemController.getItem(item.getId().toString())));
            }
        });
    }

    private ItemData fromForm(ItemForm itemForm) {
        Currency currency = Currency.valueOf(itemForm.getCurrency());
        Duration duration = Duration.of(itemForm.getDuration(), ChronoUnit.valueOf(itemForm.getDurationUnits()));
        return new ItemData(
                itemForm.getTitle(),
                itemForm.getDescription(),
                itemForm.getCurrency(),
                currency.toPriceUnits(itemForm.getIncrement().doubleValue()),
                currency.toPriceUnits(itemForm.getReserve().doubleValue()),
                duration,
                Optional.empty()); // TODO: use categories on UI
    }

    public CompletionStage<Result> editItemForm(String itemId) {
        return requireUser(ctx(), user ->
                loadNav(user).thenCompose(nav -> {
                            UUID itemUuid = UUID.fromString(itemId);
                            CompletionStage<Item> itemFuture = itemService.getItem(itemUuid).handleRequestHeader(authenticate(user)).invoke();
                            return itemFuture.thenApply(item -> {
                                        ItemForm itemForm = new ItemForm();
                                        ItemData data = item.getItemData();

                                        itemForm.setId(item.getId().toString());
                                        itemForm.setTitle(data.getTitle());
                                        itemForm.setDescription(data.getDescription());

                                        Currency currency = Currency.valueOf(data.getCurrencyId());
                                        itemForm.setCurrency(data.getCurrencyId());
                                        itemForm.setIncrement(currency.fromPriceUnits(data.getIncrement()));
                                        itemForm.setReserve(currency.fromPriceUnits(data.getReservePrice()));

                                        Pair<ChronoUnit, Long> durationDesc = Durations.fromJDuration(data.getAuctionDuration());
                                        itemForm.setDurationUnits(durationDesc.first().name());
                                        itemForm.setDuration(durationDesc.second().intValue());

                                        return ok(
                                                views.html.editItem.render(
                                                        item.getId(),
                                                        formFactory.form(ItemForm.class).fill(itemForm),
                                                        item.getStatus(),
                                                        Optional.empty(),
                                                        nav)
                                        );
                                    }
                            );
                        }
                )
        );
    }

    public CompletionStage<Result> editItem(String id, String itemStatusStr) {
        Http.Context ctx = ctx();
        return requireUser(ctx, user -> {

            Form<ItemForm> form = formFactory.form(ItemForm.class).bindFromRequest(ctx.request());
            UUID itemId = UUID.fromString(id);

            ItemStatus itemStatus = ItemStatus.valueOf(itemStatusStr);
            if (form.hasErrors()) {
                return loadNav(user).thenApply(nav ->
                        ok(views.html.editItem.render(itemId, form, itemStatus, Optional.empty(), nav))
                );
            } else {
                ItemData payload = fromForm(form.get());
                return itemService
                        .updateItem(itemId)
                        .handleRequestHeader(authenticate(user))
                        .invoke(payload)
                        .handle((updatedItem, exception) -> {
                            if (exception == null) {
                                // TODO: this is creating an extra roundtrip to the server. We should render the returned item already.
                                return CompletableFuture.completedFuture(redirect(controllers.routes.ItemController.getItem(itemId.toString())));
                            } else {
                                String msg = ((TransportException) exception.getCause()).exceptionMessage().detail();
                                return loadNav(user).thenApply(nav -> ok(
                                        editItem.render(itemId, form, itemStatus, Optional.of(msg), nav)));
                            }
                        }).thenCompose((x) -> x);
            }
        });
    }

    public CompletionStage<Result> getItem(String itemId) {
        return doGetItem(ctx(), itemId, formFactory.form(BidForm.class));
    }

    private CompletionStage<Result> doGetItem(Http.Context ctx, String itemId, Form<BidForm> bidForm) {
        return requireUser(ctx, user -> loadNav(user).thenCompose(nav -> {
            UUID itemUuid = UUID.fromString(itemId);
            CompletionStage<Item> itemFuture = itemService.getItem(itemUuid)
                    .handleRequestHeader(authenticate(user)).invoke();
            CompletionStage<PSequence<Bid>> bidHistoryFuture = bidService.getBids(itemUuid)
                    .handleRequestHeader(authenticate(user)).invoke();
            return itemFuture.thenCombineAsync(bidHistoryFuture, (item, bidHistory) -> {

                if (item.getStatus() == ItemStatus.CREATED && !item.getCreator().equals(user)) {
                    return forbidden();
                }

                User seller = null;
                Optional<User> winner = Optional.empty();

                for (User u : nav.getUsers()) {
                    if (item.getCreator().equals(u.getId())) {
                        seller = u;
                    }
                    if (item.getAuctionWinner().isPresent() && item.getAuctionWinner().get().equals(u.getId())) {
                        winner = Optional.of(u);
                    }
                }

                Optional<Integer> currentBidMaximum = Optional.empty();
                if (!bidHistory.isEmpty() && bidHistory.get(bidHistory.size() - 1).getBidder().equals(user)) {
                    currentBidMaximum = Optional.of(bidHistory.get(bidHistory.size() - 1).getMaximumPrice());
                }

                // Ensure current price is consistent with bidding history, since there's a lag between when bids
                // are placed and when the item is updated
                int currentPrice = 0;
                if (!bidHistory.isEmpty()) {
                    currentPrice = bidHistory.get(bidHistory.size() - 1).getPrice();
                }
                if (currentPrice > item.getPrice()) {
                    item = item.withPrice(currentPrice);
                }

                // Ensure that the status is consistent with the end time, since there's a lag between when the
                // auction is supposed to end, and when the bidding service actually ends it.
                if (item.getAuctionEnd().isPresent() && item.getAuctionEnd().get().isBefore(Instant.now()) &&
                        item.getStatus() == ItemStatus.AUCTION) {
                    item = item.withStatus(ItemStatus.COMPLETED);
                }

                Currency currency = Currency.valueOf(item.getItemData().getCurrencyId());

                Optional<BidResult> bidResult = loadBidResult(ctx.flash());

                return ok(views.html.item.render(item, bidForm, anonymizeBids(user, currency, bidHistory), user,
                        currency, seller, winner, currentBidMaximum, bidResult, nav));
            });
        }));
    }

    private List<AnonymousBid> anonymizeBids(UUID userId, Currency currency, List<Bid> bids) {
        List<AnonymousBid> results = new ArrayList<>();
        Map<UUID, Integer> bidders = new HashMap<>();
        bids.forEach(bid -> {
            Integer bidderNumber;
            if (userId.equals(bid.getBidder())) {
                bidderNumber = 0;
            } else {
                bidderNumber = bidders.get(bid.getBidder());
            }
            if (bidderNumber == null) {
                bidderNumber = bidders.size() + 1;
                bidders.put(bid.getBidder(), bidderNumber);
            }
            results.add(new AnonymousBid(bid.getBidTime(), bid.getPrice(), bidderNumber, bidderNumber == 0));
        });
        return results;
    }

    private Optional<BidResult> loadBidResult(Http.Flash flash) {
        String bidResultStatusString = flash.get("bidResultStatus");
        if (bidResultStatusString != null) {
            BidResultStatus status;
            try {
                status = BidResultStatus.valueOf(bidResultStatusString);
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }

            String bidResultPriceString = flash.get("bidResultPrice");
            int price = 0;
            try {
                price = Integer.parseInt(bidResultPriceString);
            } catch (NumberFormatException e) {
                // Ignore
            }
            return Optional.of(new BidResult(price, status, null));
        }
        return Optional.empty();
    }


    public CompletionStage<Result> startAuction(String itemId) {
        return requireUser(ctx(), user ->
                itemService.startAuction(UUID.fromString(itemId))
                        .handleRequestHeader(authenticate(user)).invoke().thenApply(done ->
                        redirect(routes.ItemController.getItem(itemId))
                )
        );
    }

    public CompletionStage<Result> placeBid(String itemId) {
        Http.Context ctx = ctx();
        UUID itemUuid = UUID.fromString(itemId);
        return requireUser(ctx, user -> {
            Form<BidForm> form = formFactory.form(BidForm.class).bindFromRequest(ctx.request());

            if (form.hasErrors()) {
                return doGetItem(ctx, itemId, form);
            } else {
                BidForm bidForm = form.get();

                Currency currency = Currency.valueOf(bidForm.getCurrency());
                int bidPrice = currency.toPriceUnits(bidForm.getBid().doubleValue());


                return bidService.placeBid(itemUuid)
                        .handleRequestHeader(authenticate(user))
                        .invoke(new PlaceBid(bidPrice)).thenApply(bidResult -> {
                            ctx.flash().put("bidResultStatus", bidResult.getStatus().name());
                            ctx.flash().put("bidResultPrice", Integer.toString(bidResult.getCurrentPrice()));
                            return redirect(routes.ItemController.getItem(itemId));
                        });
            }
        });
    }
}
