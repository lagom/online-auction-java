package com.example.elasticsearch;

import akka.Done;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import java.util.*;
import java.util.concurrent.CompletableFuture;


public class ElasticsearchInMem implements Elasticsearch {

    Map<UUID, IndexedItem> itemsById = new HashMap<>();
    Map<UUID, Set<UUID>> itemsByCreator = new HashMap<>();

    @Override
    public ServiceCall<UpdateIndexItem, Done> updateIndex(String index, UUID itemId) {
        return item -> {
            itemsById.merge(itemId, item.getDoc(), this::merge);
            item.getDoc().getCreatorId().map(creatorId ->
                    itemsByCreator.computeIfAbsent(creatorId, (crid) -> new HashSet<>()).add(item.getDoc().getItemId())
            );

            return CompletableFuture.completedFuture(Done.getInstance());
        };
    }

    @Override
    public ServiceCall<QueryRoot, SearchResult> search(String index) {
        return null ;
    }

    public Map<UUID, IndexedItem>   items(){
        return itemsById;
    }

    private IndexedItem merge(IndexedItem before, IndexedItem after) {
        return new IndexedItem(after.getItemId(),
                merge(before.getCreatorId(), after.getCreatorId()),
                merge(before.getTitle(), after.getTitle()),
                merge(before.getDescription(), after.getDescription()),
                merge(before.getCurrencyId(), after.getCurrencyId()),
                (after.getIncrement().isPresent()) ? after.getIncrement() : before.getIncrement(),
                (after.getPrice().isPresent()) ? after.getPrice() : before.getPrice(),
                merge(before.getStatus(), after.getStatus()),
                merge(before.getAuctionStart(), after.getAuctionStart()),
                merge(before.getAuctionEnd(), after.getAuctionEnd()),
                merge(before.getWinner(), after.getWinner()))
                ;
    }

    private <T> Optional<T> merge(Optional<T> before, Optional<T> after) {
        return (after.isPresent()) ? after : before;
    }

}
