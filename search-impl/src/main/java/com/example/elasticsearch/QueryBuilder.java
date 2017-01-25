package com.example.elasticsearch;

import com.example.auction.item.api.ItemStatus;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.Optional;
import java.util.function.Function;


public class QueryBuilder {


    private final int pageNumber;
    private final int pageSize;

    private final PSequence<Filter> filters;

    public QueryBuilder(int pageNumber, int pageSize) {
        this(pageNumber, pageSize, TreePVector.empty());
    }

    private QueryBuilder(int pageNumber, int pageSize, PSequence<Filter> filters) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.filters = filters;
    }

    public QueryBuilder withKeywords(Optional<String> keywords) {
        return withFilter(keywords, MultiMatchFilter.KeywordsFilter::new);
    }

    /**
     * @param maxPrice a normalized price as stored internally on all services.
     * @return
     */
    public QueryBuilder withMaxPrice(Optional<Integer> maxPrice, Optional<String> currency) {
        return withFilter(maxPrice, p -> new RangeFilter(new RangeFilterField.PriceRange(p)))
                .withFilter(currency, MatchFilter.CurrencyFilter::new);
    }


    private <T> QueryBuilder withFilter(Optional<T> requestField, Function<T, Filter> filterBuilder) {
        return requestField.map(fieldValue -> {
            PSequence<Filter> newFilters = filters.plus( filterBuilder.apply(fieldValue));
            return new QueryBuilder(pageNumber, pageSize, newFilters);
        }).orElse(this);
    }


    private final Filter FORBID_CREATED = new MatchFilter.ItemStatusFilter(ItemStatus.CREATED);

    public QueryRoot build() {
        return new QueryRoot(
                new Query(new BooleanQuery(FORBID_CREATED, filters)),
                pageNumber,
                pageSize
        );
    }

}
