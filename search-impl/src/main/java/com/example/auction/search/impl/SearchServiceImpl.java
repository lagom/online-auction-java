package com.example.auction.search.impl;

import com.example.auction.search.IndexedStore;
import com.example.auction.search.api.SearchItem;
import com.example.auction.search.api.SearchRequest;
import com.example.auction.search.api.SearchResult;
import com.example.auction.search.api.SearchService;
import com.example.elasticsearch.IndexedItem;
import com.example.elasticsearch.QueryBuilder;
import com.example.elasticsearch.QueryRoot;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import java.util.UUID;
import java.util.stream.Collectors;


public class SearchServiceImpl implements SearchService {


    private IndexedStore indexedStore;

    @Inject
    public SearchServiceImpl(IndexedStore indexedStore) {
        this.indexedStore = indexedStore;
    }

    @Override
    public ServiceCall<SearchRequest, SearchResult> search(int pageNo, int pageSize) {
        return req -> {
            QueryRoot query = new QueryBuilder(pageNo, pageSize)
                    .withKeywords(req.getKeywords())
                    .withMaxPrice(req.getMaxPrice(), req.getCurrency())
                    .build();
            return indexedStore.search(query).thenApply(result -> {
                TreePVector<SearchItem> items = TreePVector.from(
                        result.getIndexedItem()
                                // only return results with user provided data. We may have indexedItem's without
                                // user defined data because sometimes bid service events will arrive before the
                                // item service events.
                                .filter(ii -> ii.getCreatorId().isPresent() &&
                                        ii.getTitle().isPresent() &&
                                        ii.getDescription().isPresent() &&
                                        ii.getCurrencyId().isPresent())
                                .map(this::toApi)
                                .collect(Collectors.toList()));

                return new SearchResult(items, query.getPageSize(), query.getPageNumber(), result.getHits().getTotal());
            });
        };
    }

    // ------------------------------------------------------------------------------------------

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

