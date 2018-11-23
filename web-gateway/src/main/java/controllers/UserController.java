package controllers;

import com.example.auction.user.api.UserLogin;
import com.example.auction.user.api.UserRegistration;
import com.example.auction.user.api.UserService;
import com.typesafe.config.Config;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class UserController extends AbstractController {

    private final UserService userService;
    private final FormFactory formFactory;
    private HttpExecutionContext httpExecutionContext;
    private final Boolean showInlineInstruction;

    @Inject
    public UserController(Config config, MessagesApi messagesApi, UserService userService, FormFactory formFactory, HttpExecutionContext httpExecutionContext) {
        super(messagesApi, userService);
        this.userService = userService;
        this.formFactory = formFactory;
        this.httpExecutionContext = httpExecutionContext;
        showInlineInstruction = config.getBoolean("online-auction.instruction.show");
    }

    public CompletionStage<Result> createUserForm(final Http.Request request) {
        return withUser(request.session(), userId ->
            loadNav(userId).thenApplyAsync(nav -> {
                    Messages messages = messagesApi.preferred(request);
                    Form<CreateUserForm> userForm = formFactory.form(CreateUserForm.class);
                    return ok(views.html.createUser.render(showInlineInstruction, userForm, nav, messages));
                },
                httpExecutionContext.current())
        );
    }

    public CompletionStage<Result> createUser(final Http.Request request) {
        return withUser(request.session(), userId ->
            loadNav(userId)
                .thenComposeAsync(nav -> {
                    Messages messages = messagesApi.preferred(request);
                    Form<CreateUserForm> form = formFactory.form(CreateUserForm.class).bindFromRequest(request);
                    if (form.hasErrors()) {
                        return CompletableFuture.completedFuture(ok(views.html.createUser.render(showInlineInstruction, form, nav, messages)));
                    }

                    return userService.createUser()
                        .invoke(new UserRegistration(form.get().getName(), form.get().getEmail(), form.get().getPassword()))
                        .thenApplyAsync(user -> {
                            Http.Session session = request.session().adding("user", user.getId().toString());
                            return redirect(ProfileController.defaultProfilePage()).withSession(session);
                        }, httpExecutionContext.current());
                }, httpExecutionContext.current())
        );
    }

    public CompletionStage<Result> logoutUser(final Http.Request request) {
        return loadNav(Optional.empty())
            .thenApplyAsync(nav -> ok(views.html.index.render(nav, messagesApi.preferred(request))).withNewSession(), httpExecutionContext.current());
    }

    public Result currentUser(final Http.Request request, String userId) {
        return ok("User switched").withSession(request.session().adding("user", userId));
    }

    public CompletionStage<Result> loginUser(final Http.Request request) {
        return withUser(request.session(), userId ->
            loadNav(userId).thenComposeAsync(
                nav -> {
                    Messages messages = messagesApi.preferred(request);
                    Form<LoginForm> form = formFactory.form(LoginForm.class).bindFromRequest(request);
                    if (form.hasErrors()) {
                        return CompletableFuture.completedFuture(ok(views.html.login.render(showInlineInstruction, form, nav, messages)));
                    }

                    return userService.login().invoke(new UserLogin(form.get().getEmail(), form.get().getPassword())).thenApply(id -> {
                        Http.Session session = request.session().adding("user", id);
                        return redirect(ProfileController.defaultProfilePage()).withSession(session);
                    });
                },
                httpExecutionContext.current())
        );



    }
    public CompletionStage<Result> loginUserForm(final Http.Request request) {
        return withUser(request.session(), userId ->
            loadNav(userId)
                .thenApplyAsync(
                    nav -> {
                        Messages messages = messagesApi.preferred(request);
                        return ok(
                            views.html.login.render(
                                showInlineInstruction,
                                formFactory.form(LoginForm.class), nav, messages
                            )
                        );
                    },
                    httpExecutionContext.current()
                )
        );
    }
}
