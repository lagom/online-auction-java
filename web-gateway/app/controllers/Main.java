package controllers;

import com.example.auction.user.api.User;
import com.example.auction.user.api.UserRegistration;
import com.example.auction.user.api.UserService;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Http;
import play.mvc.Result;
import play.data.Form;
import play.Configuration;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Main extends AbstractController {

    private final UserService userService;
    private final FormFactory formFactory;

    private final Boolean showInlineInstruction;

    @Inject
    public Main(Configuration config, MessagesApi messagesApi, UserService userService, FormFactory formFactory) {
        super(messagesApi, userService);
        this.userService = userService;
        this.formFactory = formFactory;

        showInlineInstruction = config.getBoolean("online-auction.instruction.show");
    }

    public CompletionStage<Result> index() {
        return withUser(ctx(), userId ->
                loadNav(userId).thenApply(nav ->
                        ok(views.html.index.render(nav))
                )
        );
    }

}
