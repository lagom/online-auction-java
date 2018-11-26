package controllers;

import com.example.auction.user.api.UserService;
import play.i18n.MessagesApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class Main extends AbstractController {

    private HttpExecutionContext ec;

    @Inject
    public Main(MessagesApi messagesApi, UserService userService, HttpExecutionContext ec) {
        super(messagesApi, userService);
        this.ec = ec;
    }

    public CompletionStage<Result> index(final Http.Request request) {
        return withUser(request.session(), userId ->
                loadNav(userId, request).thenApplyAsync(nav ->
                                ok(views.html.index.render(nav, messagesApi.preferred(request))),
                        ec.current())
        );
    }

}
