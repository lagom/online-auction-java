package controllers;

import com.example.auction.user.api.User;
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
    public Main(Configuration config, MessagesApi messagesApi, UserService userService, WebJarAssets webJarAssets,
            FormFactory formFactory) {
        super(messagesApi, userService, webJarAssets);
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

    public CompletionStage<Result> createUserForm() {
        return withUser(ctx(), userId ->
                loadNav(userId).thenApply(nav ->
                        ok(views.html.createUser.render(showInlineInstruction, formFactory.form(CreateUserForm.class), nav))
                )
        );
    }

    public CompletionStage<Result> createUser() {
        Http.Context ctx = ctx();
        return withUser(ctx, userId ->
                loadNav(userId).thenCompose(nav -> {
                    Form<CreateUserForm> form = formFactory.form(CreateUserForm.class).bindFromRequest(ctx.request());
                    if (form.hasErrors()) {
                        return CompletableFuture.completedFuture(ok(views.html.createUser.render(showInlineInstruction, form, nav)));
                    }

                    return userService.createUser().invoke(new User(form.get().getName())).thenApply(user -> {
                        ctx.session().put("user", user.getId().toString());
                        return mergeContext(ctx, redirect(ProfileController.defaultProfilePage()));
                    });
                })
        );
    }

    public Result currentUser(String userId) {
        session("user", userId);
        return mergeContext(ctx(), ok("User switched"));
    }
}
