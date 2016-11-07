package controllers;

import com.example.auction.item.api.ItemService;
import com.example.auction.item.api.ItemStatus;
import com.example.auction.item.api.ItemSummary;
import com.example.auction.item.api.PaginatedSequence;
import com.example.auction.user.api.UserService;
import play.i18n.MessagesApi;
import play.mvc.Call;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class ProfileController extends AbstractController {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;

    private final ItemService itemService;

    @Inject
    public ProfileController(MessagesApi messagesApi, UserService userService, ItemService itemService) {
        super(messagesApi, userService);
        this.itemService = itemService;
    }

    public CompletionStage<Result> myItems(String statusParam, int page, int pageSize) {
        ItemStatus status = ItemStatus.valueOf(statusParam.toUpperCase(Locale.ENGLISH));
        return requireUser(ctx(),
                userId -> loadNav(userId).thenCombineAsync(
                        getItemsForUser(userId, status, page, pageSize), (nav, items) ->
                                ok(views.html.myItems.render(status, items, nav))
                )
        );
    }

    public static Call defaultProfilePage() {
        return profilePage(ItemStatus.CREATED);
    }

    public static Call profilePage(ItemStatus status) {
        return profilePage(status, DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
    }

    public static Call profilePage(ItemStatus status, int page, int pageSize) {
        return routes.ProfileController.myItems(status.name().toLowerCase(Locale.ENGLISH), page, pageSize);
    }

    private CompletionStage<PaginatedSequence<ItemSummary>> getItemsForUser(
            UUID userId, ItemStatus status, int page, int pageSize) {
        return itemService
                .getItemsForUser(userId, status, Optional.of(page), Optional.of(pageSize))
                .invoke();
    }
}
