package controllers;

import com.example.auction.bidding.api.BiddingService;
import com.example.auction.item.api.ItemService;
import com.example.auction.transaction.api.TransactionService;
import com.example.auction.user.api.UserService;
import play.Configuration;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

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

    public CompletionStage<Result> submitDeliveryDetailsForm() {
        return requireUser(ctx(), user ->
                loadNav(user).thenApply(nav ->
                        ok(views.html.createItem.render(showInlineInstruction, formFactory.form(DeliveryDetailsForm.class).fill(new DeliveryDetailsForm()), nav))
                )
        );
    }
}
