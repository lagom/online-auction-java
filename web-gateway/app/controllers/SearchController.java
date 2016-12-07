package controllers;

import com.example.auction.item.api.PaginatedSequence;
import com.example.auction.search.api.SearchItem;
import com.example.auction.search.api.SearchRequest;
import com.example.auction.search.api.SearchService;
import com.example.auction.user.api.UserService;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Http;
import play.mvc.Result;
import views.html.searchItem;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 *
 */
public class SearchController extends AbstractController {

    public static final int DEFAULT_PAGE_SIZE = 15;

    private final FormFactory formFactory;
    private final SearchService searchService;

    @Inject
    public SearchController(MessagesApi messagesApi,
                            UserService userService,
                            FormFactory formFactory,
                            SearchService searchService) {
        super(messagesApi, userService);
        this.formFactory = formFactory;
        this.searchService = searchService;
    }

    public CompletionStage<Result> searchForm() {
        Http.Context ctx = ctx();
        Form<SearchItemForm> form = formFactory.form(SearchItemForm.class).bindFromRequest(ctx.request());

        return loadNav(Optional.empty()).thenApply(nav ->
                ok(views.html.searchItem.render(form, Optional.empty(), nav))
        );
    }

    public CompletionStage<Result> search() {
        Http.Context ctx = ctx();
        Form<SearchItemForm> form = formFactory.form(SearchItemForm.class).bindFromRequest(ctx.request());
        return withUser(ctx, maybeUser ->
                loadNav(maybeUser).thenCompose(nav -> {
                            if (form.hasErrors()) {
                                return CompletableFuture.completedFuture(ok(views.html.searchItem.render(form, Optional.empty(), nav)));
                            } else {
                                SearchRequest searchRequest = new SearchRequest(Optional.of(form.get().getKeywords()));
                                return searchService
                                        .search(form.get().getPageNumber(), DEFAULT_PAGE_SIZE)
                                        .invoke(searchRequest)
                                        .thenApply(searchResult -> {
                                                    PaginatedSequence<SearchItem> page =
                                                            new PaginatedSequence<>(searchResult.getItems(),
                                                                    searchResult.getPageNo(),
                                                                    searchResult.getPageSize(),
                                                                    searchResult.getNumResults());
                                                    return ok(searchItem.render(form, Optional.of(page), nav));
                                                }
                                        ).exceptionally(exception ->
                                                ok(views.html.searchItem.render(form, Optional.empty(), nav))
                                        );
                            }
                        }
                )
        );
    }
}
