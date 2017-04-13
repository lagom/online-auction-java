package controllers;

import com.example.auction.pagination.PaginatedSequence;
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
import play.Configuration;

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

    private final Boolean showInlineInstruction;

    @Inject
    public SearchController(Configuration config,
                            MessagesApi messagesApi,
                            UserService userService,
                            FormFactory formFactory,
                            SearchService searchService) {
        super(messagesApi, userService);
        this.formFactory = formFactory;
        this.searchService = searchService;

        showInlineInstruction = config.getBoolean("online-auction.instruction.show");
    }

    public CompletionStage<Result> searchForm() {
        Http.Context ctx = ctx();
        Form<SearchItemForm> form = formFactory.form(SearchItemForm.class).bindFromRequest(ctx.request());

        return loadNav(Optional.empty()).thenApply(nav ->
                ok(views.html.searchItem.render(showInlineInstruction, form, Optional.empty(), nav))
        );
    }

    public CompletionStage<Result> search() {
        Http.Context ctx = ctx();
        Form<SearchItemForm> form = formFactory.form(SearchItemForm.class).bindFromRequest(ctx.request());
        return withUser(ctx, maybeUser ->
                loadNav(maybeUser).thenCompose(nav -> {
                            if (form.hasErrors()) {
                                return CompletableFuture.completedFuture(ok(views.html.searchItem.render(showInlineInstruction, form, Optional.empty(), nav)));
                            } else {

                                SearchItemForm searchItemForm = form.get();

                                int pageNumber = searchItemForm.getPageNumber();

                                return searchService
                                        .search(pageNumber, DEFAULT_PAGE_SIZE)
                                        .invoke(buildSearchRequest(searchItemForm))
                                        .thenApply(searchResult -> {
                                                    PaginatedSequence<SearchItem> page =
                                                            new PaginatedSequence<>(searchResult.getItems(),
                                                                    searchResult.getPage(),
                                                                    searchResult.getPageSize(),
                                                                    searchResult.getCount());
                                                    return ok(searchItem.render(showInlineInstruction, form, Optional.of(page), nav));
                                                }
                                        ).exceptionally(exception ->
                                                ok(views.html.searchItem.render(showInlineInstruction, form, Optional.empty(), nav))
                                        );
                            }
                        }
                )
        );
    }

    private SearchRequest buildSearchRequest(SearchItemForm searchItemForm) {

        // keywords
        String trimmedKw = searchItemForm.getKeywords().trim();
        Optional<String> keywords = Optional.empty();
        if (!trimmedKw.isEmpty()) {
            keywords = Optional.of(trimmedKw);
        }

        // max Price
        double maxPriceInput = searchItemForm.getMaximumPrice().doubleValue();
        Optional<Integer> maxPrice = Optional.empty();
        Currency currency = Currency.valueOf(searchItemForm.getMaximumPriceCurrency());
        if (maxPriceInput > 0) {
            maxPrice = Optional.of(currency.toPriceUnits(maxPriceInput));
        }


        return new SearchRequest(keywords, maxPrice, maxPrice.map(i -> currency.name()));
    }
}
