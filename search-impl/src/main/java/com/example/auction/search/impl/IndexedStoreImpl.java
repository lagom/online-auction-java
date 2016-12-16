package com.example.auction.search.impl;

import akka.Done;
import com.example.auction.search.IndexedStore;
import com.example.elasticsearch.*;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 *
 */
public class IndexedStoreImpl implements IndexedStore{

    public static final String INDEX_NAME = "auction-items";

    private final Elasticsearch elasticsearch;

    @Inject
    public IndexedStoreImpl(Elasticsearch elasticsearch) {
        this.elasticsearch = elasticsearch;
    }


    public CompletionStage<Done> store(Optional<IndexedItem> document) {
        return document
                .map(doc -> elasticsearch.updateIndex(INDEX_NAME, doc.getItemId()).invoke(new UpdateIndexItem(doc)))
                .orElse(CompletableFuture.completedFuture(Done.getInstance()));
    }

    public CompletionStage<SearchResult> search(QueryRoot query) {
        return elasticsearch.search(INDEX_NAME).invoke(query);
    }
}
