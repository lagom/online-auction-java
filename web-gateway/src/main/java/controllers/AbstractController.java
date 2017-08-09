package controllers;

import com.example.auction.user.api.User;
import com.example.auction.user.api.UserService;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public abstract class AbstractController extends Controller {
    private final MessagesApi messagesApi;
    private final UserService userService;

    public AbstractController(MessagesApi messagesApi, UserService userService) {
        this.messagesApi = messagesApi;
        this.userService = userService;
    }

    protected <T> T withUser(Http.Context ctx, Function<Optional<UUID>, T> block) {
        String id = ctx.session().get("user");
        if (id != null) {
            return block.apply(Optional.of(UUID.fromString(id)));
        } else {
            return block.apply(Optional.empty());
        }
    }

    protected CompletionStage<Result> requireUser(Http.Context ctx, Function<UUID, CompletionStage<Result>> block) {
        return withUser(ctx, maybeUser -> {
            if (maybeUser.isPresent()) {
                return block.apply(maybeUser.get());
            } else {
                return CompletableFuture.completedFuture(redirect(routes.Main.index()));
            }
        });
    }

    protected CompletionStage<Nav> loadNav(UUID userId) {
        return loadNav(Optional.of(userId));
    }

    protected CompletionStage<Nav> loadNav(Optional<UUID> userId) {
        return userService.getUsers(Optional.of(0), Optional.of(10)).invoke().thenApply(users -> {
            Optional<User> currentUser = userId.flatMap(id -> {
                for (User u: users.getItems()) {
                    if (u.getId().equals(id)) {
                        return Optional.of(u);
                    }
                }
                return Optional.empty();
            });
            return new Nav(messagesApi.preferred(Collections.emptyList()), users.getItems(), currentUser);
        });
    }

}
