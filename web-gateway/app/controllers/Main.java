package controllers;

import com.example.auction.user.api.User;
import com.example.auction.user.api.UserService;
import play.data.FormFactory;
import play.mvc.Http;
import play.mvc.Result;
import play.data.Form;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Main extends AbstractController {

    private final UserService userService;
    private final FormFactory formFactory;

    @Inject
    public Main(UserService userService, FormFactory formFactory) {
        super(userService);
        this.userService = userService;
        this.formFactory = formFactory;
    }

    public CompletionStage<Result> index() {
        return withUser(userId ->
                loadNav(userId).thenApply(nav ->
                        ok(views.html.index.render(nav))
                )
        );
    }

    public CompletionStage<Result> createUserForm() {
        return withUser(userId ->
                loadNav(userId).thenApply(nav ->
                        ok(views.html.createUser.render(formFactory.form(CreateUserForm.class), nav))
                )
        );
    }

    public CompletionStage<Result> createUser() {
        Http.Session session = session();
        Http.Request request = request();
        return withUser(userId ->
                loadNav(userId).thenCompose(nav -> {
                    Form<CreateUserForm> form = formFactory.form(CreateUserForm.class).bindFromRequest(request);
                    if (form.hasErrors()) {
                        return CompletableFuture.completedFuture(ok(views.html.createUser.render(form, nav)));
                    }

                    return userService.createUser().invoke(new User(form.get().getName())).thenApply(user -> {
                        session.put("user", user.getId().toString());
                        return redirect(routes.Main.index());
                    });
                })
        );
    }

    public Result currentUser(String userId) {
        session("user", userId);
        return ok("User switched");
    }

}
