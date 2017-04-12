package controllers;

import com.example.auction.item.api.ItemService;
import com.example.auction.item.api.ItemStatus;
import com.example.auction.item.api.ItemSummary;
import com.example.auction.pagination.PaginatedSequence;
import com.example.auction.user.api.UserService;
import play.i18n.MessagesApi;
import play.mvc.Call;
import play.mvc.Result;
import play.Configuration;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletionStage;

public class ProfileController extends AbstractController {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 15;

    private final ItemService itemService;

    private final Boolean showInlineInstruction;

    @Inject
    public ProfileController(Configuration config, MessagesApi messagesApi, UserService userService,
            WebJarAssets webJarAssets, ItemService itemService) {
        super(messagesApi, userService, webJarAssets);
        this.itemService = itemService;

        showInlineInstruction = config.getBoolean("play.instruction.show");
    }

    public CompletionStage<Result> myItems(String statusParam, int page, int pageSize) {
        ItemStatus status = ItemStatus.valueOf(statusParam.toUpperCase(Locale.ENGLISH));
        return requireUser(ctx(),
                userId -> loadNav(userId).thenCombineAsync(
                        getItemsForUser(userId, status, page, pageSize), (nav, items) ->
                                ok(views.html.myItems.render(showInlineInstruction, status, items, nav))
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
        Map<String, String> queryParams = new LinkedHashMap<>();
        if (page != ProfileController.DEFAULT_PAGE) {
            queryParams.put("page", Integer.toString(page));
        }
        if (pageSize != ProfileController.DEFAULT_PAGE_SIZE) {
            queryParams.put("pageSize", Integer.toString(pageSize));
        }
        String path = String.format("/my/items/%s", status);

        if (queryParams.isEmpty()) {
            return new play.api.mvc.Call("GET", path, null);
        } else {
            StringJoiner joiner = new StringJoiner(",", path + "?", "");
            queryParams.forEach((key, value) -> joiner.add(key + "=" + value));
            return new play.api.mvc.Call("GET", joiner.toString(), null);
        }
    }

    private CompletionStage<PaginatedSequence<ItemSummary>> getItemsForUser(
            UUID userId, ItemStatus status, int page, int pageSize) {
        return itemService
                .getItemsForUser(userId, status, Optional.of(page), Optional.of(pageSize))
                .invoke();
    }
}
