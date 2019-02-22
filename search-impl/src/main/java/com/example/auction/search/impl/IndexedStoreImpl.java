package com.example.auction.search.impl;

import akka.Done;
import com.example.auction.search.IndexedStore;
import com.example.elasticsearch.IndexedItem;
import com.example.elasticsearch.ItemSearchResult;
import org.taymyr.lagom.elasticsearch.document.ElasticDocument;
import org.taymyr.lagom.elasticsearch.document.dsl.update.DocUpdateRequest;
import org.taymyr.lagom.elasticsearch.search.ElasticSearch;
import org.taymyr.lagom.elasticsearch.search.dsl.SearchRequest;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;

import static akka.Done.done;
import static org.taymyr.lagom.elasticsearch.ServiceCall.invoke;

import static java.util.concurrent.CompletableFuture.completedFuture;

/**
 *
 */
public class IndexedStoreImpl implements IndexedStore {

    public static final String INDEX_NAME = "auction";
    public static final String TYPE_NAME = "item";

    private final ElasticSearch elasticSearch;
    private final ElasticDocument elasticDocument;

    @Inject
    public IndexedStoreImpl(ElasticSearch elasticSearch, ElasticDocument elasticDocument) {
        this.elasticSearch = elasticSearch;
        this.elasticDocument = elasticDocument;
    }


    public CompletionStage<Done> store(Optional<IndexedItem> document) {
        return document
            .map(
                doc -> invoke(
                    elasticDocument.update(INDEX_NAME, TYPE_NAME, doc.getItemId().toString()),
                    DocUpdateRequest.builder().docAsUpsert(true).doc(doc).build()
                ).thenApply(updateResult -> done())
            )
            .orElse(completedFuture(done()));
    }

    public CompletionStage<ItemSearchResult> search(SearchRequest query) {
        return invoke(elasticSearch.search(INDEX_NAME), query, ItemSearchResult.class);
    }
}
