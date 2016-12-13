package com.example.auction.search;

import akka.Done;
import com.example.elasticsearch.*;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 *
 */
public interface IndexedStore {

    CompletionStage<Done> store(Optional<IndexedItem> document);

    CompletionStage<SearchResult> search(QueryRoot query);
}
