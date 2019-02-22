package com.example.auction.search.impl;

import com.example.auction.item.api.ItemStatus;
import com.example.auction.pagination.PaginatedSequence;
import com.example.auction.search.IndexedStore;
import com.example.auction.search.api.SearchItem;
import com.example.auction.search.api.SearchService;
import com.example.elasticsearch.IndexedItem;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.TreePVector;
import org.taymyr.lagom.elasticsearch.search.dsl.SearchRequest;
import org.taymyr.lagom.elasticsearch.search.dsl.query.compound.BoolQuery;
import org.taymyr.lagom.elasticsearch.search.dsl.query.fulltext.MatchQuery;
import org.taymyr.lagom.elasticsearch.search.dsl.query.fulltext.MultiMatchQuery;
import org.taymyr.lagom.elasticsearch.search.dsl.query.term.NumericRange;
import org.taymyr.lagom.elasticsearch.search.dsl.query.term.RangeQuery;

import java.util.UUID;
import java.util.stream.Collectors;
import javax.inject.Inject;


public class SearchServiceImpl implements SearchService {


    private IndexedStore indexedStore;

    @Inject
    public SearchServiceImpl(IndexedStore indexedStore) {
        this.indexedStore = indexedStore;
    }

    @Override
    public ServiceCall<com.example.auction.search.api.SearchRequest, PaginatedSequence<SearchItem>> search(int pageNo, int pageSize) {
        return req -> {
            SearchRequest searchRequest = SearchRequest.builder()
                .query(toBoolQuery(req))
                .from(pageNo * pageSize)
                .size(pageSize)
                .build();
            return indexedStore.search(searchRequest).thenApply(result -> {
                TreePVector<SearchItem> items = TreePVector.from(
                        result.getSources().stream()
                                // only return results with user provided data. We may have indexedItem's without
                                // user defined data because sometimes bid service events will arrive before the
                                // item service events.
                                .filter(ii -> ii.getCreatorId().isPresent() &&
                                        ii.getTitle().isPresent() &&
                                        ii.getDescription().isPresent() &&
                                        ii.getCurrencyId().isPresent())
                                .map(this::toApi)
                                .collect(Collectors.toList()));

                return new PaginatedSequence<>(items, pageNo, pageSize, result.getHits().getTotal());
            });
        };
    }

    // ------------------------------------------------------------------------------------------
    private BoolQuery toBoolQuery(com.example.auction.search.api.SearchRequest req) {
        BoolQuery.Builder boolQueryBuilder = BoolQuery.builder().mustNot(
            MatchQuery.of("status", ItemStatus.CREATED.name())
        );
        req.getKeywords().ifPresent(keywords -> boolQueryBuilder.must(
            MultiMatchQuery.builder()
                .query(keywords)
                .fields("title", "description")
                .build()
        ));
        req.getMaxPrice().ifPresent(maxPrice -> boolQueryBuilder.must(
            RangeQuery.of("price", NumericRange.lte(maxPrice))
        ));
        req.getCurrency().ifPresent(currency -> boolQueryBuilder.must(
            MatchQuery.builder().field("currencyId").query(currency).build()
        ));
        return boolQueryBuilder.build();
    }

    private SearchItem toApi(IndexedItem indexedItem) {
        return new SearchItem(
                indexedItem.getItemId(),
                indexedItem.getCreatorId().orElse(UUID.randomUUID()),
                indexedItem.getTitle().get(),
                indexedItem.getDescription().get(),
                indexedItem.getStatus().get().name(),
                indexedItem.getCurrencyId().get(),
                indexedItem.getPrice(),
                indexedItem.getAuctionStart(),
                indexedItem.getAuctionEnd()
        );
    }
}

