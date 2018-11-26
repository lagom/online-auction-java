package controllers;

import akka.japi.Pair;
import com.example.auction.bidding.api.*;
import com.example.auction.item.api.Item;
import com.example.auction.item.api.ItemData;
import com.example.auction.item.api.ItemService;
import com.example.auction.item.api.ItemStatus;
import com.example.auction.user.api.UserService;
import com.lightbend.lagom.javadsl.api.transport.TransportException;
import com.typesafe.config.Config;
import org.pcollections.PSequence;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import views.html.editItem;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.example.auction.security.ClientSecurity.authenticate;

public class ItemController extends AbstractController {

    private final FormFactory formFactory;
    private final ItemService itemService;
    private final BiddingService bidService;
    private final Boolean showInlineInstruction;
    private HttpExecutionContext ec;

    @Inject
    public ItemController(Config config,
                          MessagesApi messagesApi,
                          UserService userService,
                          FormFactory formFactory,
                          ItemService itemService,
                          BiddingService bidService,
                          HttpExecutionContext ec) {
        super(messagesApi, userService);
        this.formFactory = formFactory;
        this.itemService = itemService;
        this.bidService = bidService;

        showInlineInstruction = config.getBoolean("online-auction.instruction.show");
        this.ec = ec;
    }

    public CompletionStage<Result> createItemForm(final Http.Request request) {
        return requireUser(request.session(), user ->
                loadNav(user, request)
                    .thenApplyAsync(nav -> {
                            Messages messages = messagesApi.preferred(request);
                            Form<ItemForm> itemForm = formFactory.form(ItemForm.class).fill(new ItemForm());
                            return ok(views.html.createItem.render(showInlineInstruction, itemForm, nav, messages));
                        },
                        ec.current())
        );
    }

    public CompletionStage<Result> createItem(final Http.Request request) {
        return requireUser(request.session(), user -> {

            Form<ItemForm> form = formFactory.form(ItemForm.class).bindFromRequest(request);

            if (form.hasErrors()) {
                return loadNav(user, request).thenApplyAsync(nav ->
                                ok(views.html.createItem.render(showInlineInstruction, form, nav, messagesApi.preferred(request))),
                        ec.current());
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

    public CompletionStage<Result> editItemForm(final Http.Request request, String itemId) {
        return requireUser(request.session(), user ->
                loadNav(user, request).thenCompose(nav -> {
                            Messages messages = messagesApi.preferred(request);
                            UUID itemUuid = UUID.fromString(itemId);
                            CompletionStage<Item> itemFuture = itemService.getItem(itemUuid).handleRequestHeader(authenticate(user)).invoke();
                            return itemFuture.thenApplyAsync(item -> {
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
                                                    showInlineInstruction,
                                                    item.getId(),
                                                    formFactory.form(ItemForm.class).fill(itemForm),
                                                    item.getStatus(),
                                                    Optional.empty(),
                                                    nav,
                                                    messages
                                                )
                                        );
                                    },
                                    ec.current());
                        }
                )
        );
    }

    public CompletionStage<Result> editItem(final Http.Request request, String id, String itemStatusStr) {
        return requireUser(request.session(), user -> {

            Form<ItemForm> form = formFactory.form(ItemForm.class).bindFromRequest(request);
            UUID itemId = UUID.fromString(id);

            ItemStatus itemStatus = ItemStatus.valueOf(itemStatusStr);
            if (form.hasErrors()) {
                return loadNav(user, request).thenApply(nav ->
                        ok(views.html.editItem.render(
                            showInlineInstruction,
                            itemId,
                            form,
                            itemStatus,
                            Optional.empty(),
                            nav,
                            messagesApi.preferred(request)
                        ))
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
                                return loadNav(user, request).thenApply(nav -> ok(
                                        editItem.render(showInlineInstruction, itemId, form, itemStatus, Optional.of(msg), nav, messagesApi.preferred(request))));
                            }
                        }).thenComposeAsync((x) -> x, ec.current());
            }
        });
    }
    public CompletionStage<Result> getItem(final Http.Request request, String itemId) {
        return doGetItem(request, itemId, formFactory.form(BidForm.class));
    }

    private CompletionStage<Result> doGetItem(final Http.Request request, String itemId, Form<BidForm> bidForm) {
        return requireUser(request.session(), user -> loadNav(user, request)
            .thenComposeAsync(nav -> {

                UUID itemUuid = UUID.fromString(itemId);
                CompletionStage<Item> itemFuture = itemService.getItem(itemUuid)
                    .handleRequestHeader(authenticate(user)).invoke();
                CompletionStage<PSequence<Bid>> bidHistoryFuture = bidService.getBids(itemUuid)
                    .handleRequestHeader(authenticate(user)).invoke();
                return bidHistoryFuture.thenComposeAsync(bidHistory ->

                        itemFuture.thenComposeAsync(item1 ->
                        {
                            UUID sellerId = item1.getCreator();
                            Optional<UUID> winnerId = item1.getAuctionWinner();
                            return getUser(sellerId).thenCombineAsync(getUser(winnerId),
                                (seller, winner) -> {
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
                                    Item item = item1;
                                    if (currentPrice > item1.getPrice()) {
                                        item = item.withPrice(currentPrice);
                                    }

                                    // Ensure that the status is consistent with the end time, since there's a lag between when the
                                    // auction is supposed to end, and when the bidding service actually ends it.
                                    if (item.getAuctionEnd().isPresent() && item.getAuctionEnd().get().isBefore(Instant.now()) &&
                                        item.getStatus() == ItemStatus.AUCTION) {
                                        item = item.withStatus(ItemStatus.COMPLETED);
                                    }

                                    Currency currency = Currency.valueOf(item.getItemData().getCurrencyId());

                                    Optional<BidResult> bidResult = loadBidResult(request.flash());

                                    return ok(views.html.item.render(showInlineInstruction, item, bidForm,
                                        anonymizeBids(user, bidHistory), user, currency, seller, winner, currentBidMaximum, bidResult, nav, messagesApi.preferred(request)));
                                }, ec.current());
                        }, ec.current())
                    , ec.current());
            }, ec.current()));

    }

    private List<AnonymousBid> anonymizeBids(UUID userId, List<Bid> bids) {
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
        return flash.getOptional("bidResultStatus").flatMap(bidResultStatusString -> {
            BidResultStatus status;
            try {
                status = BidResultStatus.valueOf(bidResultStatusString);
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }

            final Integer price = flash.getOptional("bidResultPrice").map(bidResultPriceString -> {
                try {
                    return Integer.parseInt(bidResultPriceString);
                } catch (NumberFormatException e) {
                    // Ignore
                }
                return 0;
            }).orElse(0);

            return Optional.of(new BidResult(price, status, null));
        });
    }


    public CompletionStage<Result> startAuction(final Http.Request request, String itemId) {
        return requireUser(request.session(), user ->
                itemService.startAuction(UUID.fromString(itemId))
                        .handleRequestHeader(authenticate(user)).invoke().thenApplyAsync(done ->
                                redirect(routes.ItemController.getItem(itemId)),
                        ec.current())
        );
    }

    public CompletionStage<Result> placeBid(final Http.Request request, String itemId) {
        UUID itemUuid = UUID.fromString(itemId);
        return requireUser(request.session(), user -> {
            Form<BidForm> form = formFactory.form(BidForm.class).bindFromRequest(request);

            if (form.hasErrors()) {
                return doGetItem(request, itemId, form);
            } else {
                BidForm bidForm = form.get();

                Currency currency = Currency.valueOf(bidForm.getCurrency());
                int bidPrice = currency.toPriceUnits(bidForm.getBid().doubleValue());


                return bidService.placeBid(itemUuid)
                        .handleRequestHeader(authenticate(user))
                        .invoke(new PlaceBid(bidPrice)).thenApplyAsync(bidResult -> {
                            Http.Flash flash = request.flash()
                                .adding("bidResultStatus", bidResult.getStatus().name())
                                .adding("bidResultPrice", Integer.toString(bidResult.getCurrentPrice()));
                            return redirect(routes.ItemController.getItem(itemId)).withFlash(flash);
                        },
                        ec.current());
            }
        });
    }
}
