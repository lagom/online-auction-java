package controllers;

import com.example.auction.user.api.User;
import com.example.auction.user.api.UserService;
import com.typesafe.config.Config;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import play.data.Form;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Main extends AbstractController {

    private final UserService userService;
    private final FormFactory formFactory;

    private final Boolean showInlineInstruction;
    private HttpExecutionContext ec;

    @Inject
    public Main(Config config, MessagesApi messagesApi, UserService userService, FormFactory formFactory, HttpExecutionContext ec) {
        super(messagesApi, userService);
        this.userService = userService;
        this.formFactory = formFactory;

        showInlineInstruction = config.getBoolean("online-auction.instruction.show");
        this.ec = ec;
    }

    public CompletionStage<Result> index() {
        return withUser(ctx(), userId ->
                loadNav(userId).thenApplyAsync(nav ->
                        ok(views.html.index.render(nav))
                ,ec.current())
        );
    }

    public CompletionStage<Result> createUserForm() {
        return withUser(ctx(), userId ->
                loadNav(userId).thenApplyAsync(nav ->
                        ok(views.html.createUser.render(showInlineInstruction, formFactory.form(CreateUserForm.class), nav))
                        ,ec.current())
        );
    }

    public CompletionStage<Result> createUser() {
        Http.Context ctx = ctx();
        return withUser(ctx, userId ->
                loadNav(userId).thenComposeAsync(nav -> {
                    Form<CreateUserForm> form = formFactory.form(CreateUserForm.class).bindFromRequest(ctx.request());
                    if (form.hasErrors()) {
                        return CompletableFuture.completedFuture(ok(views.html.createUser.render(showInlineInstruction, form, nav)));
                    }

                    return userService.createUser().invoke(new User(form.get().getName())).thenApply(user -> {
                        ctx.session().put("user", user.getId().toString());
                        return redirect(ProfileController.defaultProfilePage());
                    });
                }, ec.current())
        );
    }

    public Result currentUser(String userId) {
        session("user", userId);
        return ok("User switched");
    }

}
