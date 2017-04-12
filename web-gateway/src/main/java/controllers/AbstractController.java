package controllers;

import com.example.auction.user.api.User;
import com.example.auction.user.api.UserService;
import play.core.j.JavaHelpers;
import play.core.j.JavaHelpers$;
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
    private final WebJarAssets webJarAssets;

    public AbstractController(MessagesApi messagesApi, UserService userService, WebJarAssets webJarAssets) {
        this.messagesApi = messagesApi;
        this.userService = userService;
        this.webJarAssets = webJarAssets;
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
                return CompletableFuture.completedFuture(redirect("/"));
            }
        });
    }

    protected CompletionStage<Nav> loadNav(UUID userId) {
        return loadNav(Optional.of(userId));
    }

    protected CompletionStage<Nav> loadNav(Optional<UUID> userId) {
        return userService.getUsers().invoke().thenApply(users -> {
            Optional<User> currentUser = userId.flatMap(id -> {
                for (User u: users) {
                    if (u.getId().equals(id)) {
                        return Optional.of(u);
                    }
                }
                return Optional.empty();
            });
            return new Nav(messagesApi.preferred(Collections.emptyList()), webJarAssets, currentUser, users);
        });
    }

    /**
     * Work around https://github.com/playframework/playframework/issues/7213.
     *
     * This needs to be called by any actions that modify the HTTP context, if those modifications are going to make
     * it into the final result. Once Play is upgraded to 2.5.15 or greater, this method and all invocations of it can
     * be removed.
     */
    protected Result mergeContext(Http.Context ctx, Result result) {
        return JavaHelpers$.MODULE$.createResult(ctx, result).asJava();
    }
}
