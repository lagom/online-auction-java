package controllers;

import com.example.auction.item.api.ItemService;
import com.example.auction.item.api.ItemStatus;
import com.example.auction.item.api.ItemSummary;
import com.example.auction.pagination.PaginatedSequence;
import com.example.auction.user.api.UserService;
import com.typesafe.config.Config;
import play.i18n.MessagesApi;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Call;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class ProfileController extends AbstractController {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 15;

    private final ItemService itemService;

    private final Boolean showInlineInstruction;
    private HttpExecutionContext ec;

    @Inject
    public ProfileController(Config config,
                             MessagesApi messagesApi,
                             UserService userService,
                             ItemService itemService,
                             HttpExecutionContext ec) {
        super(messagesApi, userService);
        this.itemService = itemService;

        showInlineInstruction = config.getBoolean("online-auction.instruction.show");
        this.ec = ec;
    }

    public CompletionStage<Result> myItems(final Http.Request request, String statusParam, int page, int pageSize) {
        ItemStatus status = ItemStatus.valueOf(statusParam.toUpperCase(Locale.ENGLISH));
        return requireUser(request.session(),
                userId -> loadNav(userId).thenCombineAsync(
                        getItemsForUser(userId, status, page, pageSize), (nav, items) ->
                                ok(views.html.myItems.render(showInlineInstruction, status, items, nav, messagesApi.preferred(request))),
                        ec.current())
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
