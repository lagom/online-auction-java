package controllers;

import com.example.auction.user.api.UserService;
import play.i18n.MessagesApi;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class Main extends AbstractController {

    @Inject
    public Main(MessagesApi messagesApi, UserService userService) {
        super(messagesApi, userService);
 }

    public CompletionStage<Result> index() {
        return withUser(ctx(), userId ->
                loadNav(userId).thenApply(nav ->
                        ok(views.html.index.render(nav))
                )
        );
    }

}
