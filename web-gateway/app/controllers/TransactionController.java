package controllers;

import com.example.auction.transaction.api.DeliveryInfo;
import com.example.auction.transaction.api.TransactionInfo;
import com.example.auction.transaction.api.TransactionInfoStatus;
import com.example.auction.transaction.api.TransactionService;
import com.example.auction.user.api.User;
import com.example.auction.user.api.UserService;
import com.lightbend.lagom.javadsl.api.transport.TransportException;
import play.Configuration;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.example.auction.security.ClientSecurity.authenticate;

public class TransactionController extends AbstractController {
    private final FormFactory formFactory;
    private final TransactionService transactionService;

    private final Boolean showInlineInstruction;

    @Inject
    public TransactionController(Configuration config, MessagesApi messagesApi, UserService userService, FormFactory formFactory,
                                 TransactionService transactionService) {
        super(messagesApi, userService);
        this.formFactory = formFactory;
        this.transactionService = transactionService;

        showInlineInstruction = config.getBoolean("online-auction.instruction.show");
    }

    public CompletionStage<Result> getTransaction(String id) {
        return requireUser(ctx(), user ->
                loadNav(user).thenCompose(nav -> {
                    UUID itemId = UUID.fromString(id);
                    CompletionStage<TransactionInfo> transactionFuture = transactionService.getTransaction(itemId).handleRequestHeader(authenticate(user)).invoke();
                    return transactionFuture.handle((transaction, exception) ->{
                        if(exception == null) {
                            Optional<User> seller = Optional.empty();
                            Optional<User> winner = Optional.empty();
                            for (User u : nav.getUsers()) {
                                if (transaction.getCreator().equals(u.getId())) {
                                    seller = Optional.of(u);
                                }
                                if (transaction.getWinner().equals(u.getId())) {
                                    winner = Optional.of(u);
                                }
                            }
                            Currency currency = Currency.valueOf(transaction.getItemData().getCurrencyId());
                            return ok(views.html.transaction.render(showInlineInstruction, Optional.of(transaction), seller, winner, Optional.of(currency), Optional.empty(), nav));
                        }
                        else {
                            String msg = ((TransportException) exception.getCause()).exceptionMessage().detail();
                            return ok(views.html.transaction.render(showInlineInstruction, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(msg), nav));
                        }
                    });
                })
        );
    }

    public CompletionStage<Result> submitDeliveryDetailsForm(String id) {
        return requireUser(ctx(), user ->
            loadNav(user).thenCompose(nav -> {
                UUID itemId = UUID.fromString(id);
                CompletionStage<TransactionInfo> transactionFuture = transactionService.getTransaction(itemId).handleRequestHeader(authenticate(user)).invoke();
                return transactionFuture.handle((transaction, exception) ->{
                    if(exception == null) {
                        DeliveryDetailsForm form = new DeliveryDetailsForm();
                        Optional<DeliveryInfo> maybeDeliveryInfo = transaction.getDeliveryInfo();
                        if(maybeDeliveryInfo.isPresent()) {
                            form.setAddressLine1(maybeDeliveryInfo.get().getAddressLine1());
                            form.setAddressLine2(maybeDeliveryInfo.get().getAddressLine2());
                            form.setCity(maybeDeliveryInfo.get().getCity());
                            form.setState(maybeDeliveryInfo.get().getState());
                            form.setPostalCode(maybeDeliveryInfo.get().getPostalCode());
                            form.setCountry(maybeDeliveryInfo.get().getCountry());
                        }
                        return ok(
                                views.html.deliveryDetails.render(
                                        showInlineInstruction,
                                        !transaction.getCreator().equals(user),
                                        itemId,
                                        formFactory.form(DeliveryDetailsForm.class).fill(form),
                                        transaction.getStatus(),
                                        Optional.empty(),
                                        nav)
                        );
                    }
                    else {
                        String msg = ((TransportException) exception.getCause()).exceptionMessage().detail();
                        return ok(views.html.deliveryDetails.render(showInlineInstruction, false, itemId, formFactory.form(DeliveryDetailsForm.class), TransactionInfoStatus.NEGOTIATING_DELIVERY, Optional.of(msg), nav));
                    }
                });
            })
        );
    }

    public CompletionStage<Result> submitDeliveryDetails(String id, String transactionStatus, boolean isBuyer) {
        Http.Context ctx = ctx();
        return requireUser(ctx(), user -> {

            Form<DeliveryDetailsForm> form = formFactory.form(DeliveryDetailsForm.class).bindFromRequest(ctx.request());
            UUID itemId = UUID.fromString(id);
            TransactionInfoStatus status = TransactionInfoStatus.valueOf(transactionStatus);

            if (form.hasErrors()) {
                return loadNav(user).thenApply(nav ->
                        ok(views.html.deliveryDetails.render(showInlineInstruction, isBuyer, itemId, form, status, Optional.empty(), nav))
                );
            } else {
                return transactionService.submitDeliveryDetails(itemId)
                        .handleRequestHeader(authenticate(user))
                        .invoke(fromForm(form.get()))
                        .handle((done, exception) -> {
                            if(exception == null) {
                                return CompletableFuture.completedFuture(redirect(routes.TransactionController.getTransaction(id)));
                                //return CompletableFuture.completedFuture(redirect(controllers.routes.TransactionController.submitDeliveryDetailsForm(id)));
                            } else {
                                String msg = ((TransportException) exception.getCause()).exceptionMessage().detail();
                                return loadNav(user).thenApply(nav ->
                                    ok(views.html.deliveryDetails.render(showInlineInstruction, isBuyer, itemId, form, status, Optional.of(msg), nav)));
                            }
                        }).thenCompose(x -> x);
            }
        });
    }

    private DeliveryInfo fromForm(DeliveryDetailsForm deliveryForm) {
        return new DeliveryInfo(
                deliveryForm.getAddressLine1(),
                deliveryForm.getAddressLine2(),
                deliveryForm.getCity(),
                deliveryForm.getState(),
                deliveryForm.getPostalCode(),
                deliveryForm.getCountry()
        );
    }
}
