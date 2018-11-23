package controllers;

import com.example.auction.pagination.PaginatedSequence;
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

import static com.example.auction.security.ClientSecurity.authenticate;

public abstract class AbstractController extends Controller {
    protected final MessagesApi messagesApi;
    protected final UserService userService;

    public AbstractController(MessagesApi messagesApi, UserService userService) {
        this.messagesApi = messagesApi;
        this.userService = userService;
    }

    protected <T> T withUser(Http.Session session, Function<Optional<UUID>, T> block) {
        return block.apply(session.getOptional("user").map(UUID::fromString));
    }

    protected CompletionStage<Result> requireUser(Http.Session session, Function<UUID, CompletionStage<Result>> block) {
        return withUser(session, maybeUser -> {
            if (maybeUser.isPresent()) {
                return block.apply(maybeUser.get());
            } else {
                return CompletableFuture.completedFuture(redirect(routes.Main.index()));
            }
        });
    }

    private Nav getNav(PaginatedSequence<User> users, Optional<User> currentUser) {
        return new Nav(messagesApi.preferred(Collections.emptyList()), users.getItems(), currentUser);
    }

    protected CompletionStage<Nav> loadNav(UUID userId) {
        return loadNav(Optional.of(userId));
    }

    protected CompletionStage<Optional<User>> getUser(Optional<UUID> userId) {
        if (userId.isPresent()) {
            return userService.getUser(userId.get())
                .handleRequestHeader(authenticate(userId.get()))
                .invoke()
                .thenApply(Optional::of);
        } else {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    protected CompletionStage<User> getUser(UUID userId) {
        return userService.getUser(userId)
            .handleRequestHeader(authenticate(userId))
            .invoke();
    }

    protected CompletionStage<Nav> loadNav(Optional<UUID> userId) {
        CompletionStage<Optional<User>> createdUser = getUser(userId);
        CompletionStage<PaginatedSequence<User>> users = userService.getUsers(Optional.of(0), Optional.of(10)).invoke();
        return users.thenCombineAsync(createdUser, this::getNav);
    }
}
