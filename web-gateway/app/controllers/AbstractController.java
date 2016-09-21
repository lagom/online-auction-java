package controllers;

import com.example.auction.user.api.User;
import com.example.auction.user.api.UserService;
import com.lightbend.lagom.javadsl.api.transport.RequestHeader;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public abstract class AbstractController extends Controller {
    private final UserService userService;

    public AbstractController(UserService userService) {
        this.userService = userService;
    }

    protected <T> T withUser(Function<Optional<UUID>, T> block) {
        String id = session("user");
        if (id != null) {
            return block.apply(Optional.of(UUID.fromString(id)));
        } else {
            return block.apply(Optional.empty());
        }
    }

    protected CompletionStage<Result> requireUser(Function<UUID, CompletionStage<Result>> block) {
        return withUser(maybeUser -> {
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
        return userService.getUsers().invoke().thenApply(users -> {
            Optional<User> currentUser = userId.flatMap(id -> {
                for (User u: users) {
                    if (u.getId().equals(id)) {
                        return Optional.of(u);
                    }
                }
                return Optional.empty();
            });
            return new Nav(users, currentUser);
        });
    }

}
