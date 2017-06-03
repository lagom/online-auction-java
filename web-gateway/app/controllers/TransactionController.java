package controllers;

import com.example.auction.transaction.api.DeliveryInfo;
import com.example.auction.transaction.api.TransactionService;
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

    public CompletionStage<Result> submitDeliveryDetailsForm(String id) {
        return requireUser(ctx(), user ->
                loadNav(user).thenApply(nav -> {
                            UUID itemId = UUID.fromString(id);
                            // TODO: Fill with deliveryForm with data
                            return ok(
                                    views.html.deliveryDetails.render(
                                            showInlineInstruction,
                                            itemId,
                                            formFactory.form(DeliveryDetailsForm.class).fill(new DeliveryDetailsForm()),
                                            Optional.empty(),
                                            nav)
                            );

                        }
                )
        );
    }

    public CompletionStage<Result> submitDeliveryDetails(String id) {
        Http.Context ctx = ctx();
        return requireUser(ctx(), user -> {

            Form<DeliveryDetailsForm> form = formFactory.form(DeliveryDetailsForm.class).bindFromRequest(ctx.request());
            UUID itemId = UUID.fromString(id);

            if (form.hasErrors()) {
                return loadNav(user).thenApply(nav ->
                        ok(views.html.deliveryDetails.render(showInlineInstruction, itemId, form, Optional.empty(), nav))
                );
            } else {
                return transactionService.submitDeliveryDetails(itemId)
                        .handleRequestHeader(authenticate(user))
                        .invoke(fromForm(form.get()))
                        .handle((done, exception) -> {
                            if(exception == null) {
                                return CompletableFuture.completedFuture(redirect(controllers.routes.TransactionController.submitDeliveryDetailsForm(id)));
                            } else {
                                String msg = ((TransportException) exception.getCause()).exceptionMessage().detail();
                                return loadNav(user).thenApply(nav -> ok(
                                        views.html.deliveryDetails.render(showInlineInstruction, itemId, form, Optional.of(msg), nav)));
                            }
                        }).thenCompose((x) -> x);
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
