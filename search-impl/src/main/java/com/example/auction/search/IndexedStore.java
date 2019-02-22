package com.example.auction.search;

import akka.Done;
import com.example.elasticsearch.*;
import org.taymyr.lagom.elasticsearch.search.dsl.SearchRequest;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 *
 */
public interface IndexedStore {

    CompletionStage<Done> store(Optional<IndexedItem> document);

    CompletionStage<ItemSearchResult> search(SearchRequest query);
}
