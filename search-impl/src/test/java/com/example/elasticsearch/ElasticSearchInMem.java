package com.example.elasticsearch;

import akka.Done;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 *
 */
public class ElasticSearchInMem implements ElasticSearch {

    Map<UUID, IndexedItem> itemsById = new HashMap<>();
    Map<UUID, Set<UUID>> itemsByCreator = new HashMap<>();

    @Override
    public ServiceCall<IndexedItem, Done> updateIndex(String index, UUID itemId) {
        return item -> {
            itemsById.merge(itemId, item, this::merge);
            item.getCreatorId().map(creatorId ->
                    itemsByCreator.computeIfAbsent(creatorId, (crid) -> new HashSet<>()).add(item.getItemId())
            );

            return CompletableFuture.completedFuture(Done.getInstance());
        };
    }

    @Override
    public ServiceCall<QueryRoot, PSequence<IndexedItem>> search(String index) {
        return q -> {
            itemsById.values().stream().forEach(System.out::println);
            return CompletableFuture.completedFuture(itemsById.values().stream())
                    .thenApply(xs -> xs.filter(q::test))
                    .thenApply(xs -> xs.collect(Collectors.toList()))
                    .thenApply(TreePVector::from);
        };
    }

    private IndexedItem merge(IndexedItem before, IndexedItem after) {
        return new IndexedItem(after.getItemId())
                .withCreatorId(merge(after.getCreatorId(), before.getCreatorId()))
                .withTitle(merge(after.getTitle(), before.getTitle()))
                .withDescription(merge(after.getDescription(), before.getDescription()))
                .withCurrencyId(merge(after.getCurrencyId(), before.getCurrencyId()))
                .withIncrement((after.getIncrement().isPresent()) ? after.getIncrement() : before.getIncrement())
                .withPrice((after.getPrice().isPresent()) ? after.getPrice() : before.getPrice())
                .withStatus(merge(after.getStatus(), before.getStatus()))
                .withAuctionStart(merge(after.getAuctionStart(), before.getAuctionStart()))
                .withAuctionEnd(merge(after.getAuctionEnd(), before.getAuctionEnd()))
                .withWinner(merge(after.getWinner(), before.getWinner()))
                ;
    }

    private <T> Optional<T> merge(Optional<T> before, Optional<T> after) {
        return (after.isPresent()) ? after : before;
    }

}
