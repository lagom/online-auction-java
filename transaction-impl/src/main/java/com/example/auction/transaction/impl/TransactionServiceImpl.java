package com.example.auction.transaction.impl;

import akka.Done;
import akka.stream.javadsl.Flow;
import com.example.auction.item.api.Item;
import com.example.auction.item.api.ItemEvent;
import com.example.auction.item.api.ItemService;
import com.example.auction.transaction.impl.TransactionCommand.*;
import com.example.auction.transaction.api.TransactionService;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TransactionServiceImpl implements TransactionService {

    private final PersistentEntityRegistry registry;

    @Inject
    public TransactionServiceImpl(PersistentEntityRegistry registry, ItemService itemService) {
        this.registry = registry;
        registry.register(TransactionEntity.class);
        // Subscribe to the events from the item service.
        itemService.itemEvents().subscribe().atLeastOnce(Flow.<ItemEvent>create().mapAsync(1, itemEvent -> {
            if (itemEvent instanceof ItemEvent.AuctionFinished) {
                ItemEvent.AuctionFinished auctionFinished = (ItemEvent.AuctionFinished) itemEvent;
                Item item = auctionFinished.getItem();
                Transaction transaction = new Transaction(item.getId(), item.getCreator(),
                        item.getAuctionWinner().get(), item.getPrice(), 0);

                return entityRef(auctionFinished.getItemId()).ask(new StartTransaction(transaction));
            } else {
                return CompletableFuture.completedFuture(Done.getInstance());
            }
        }));
    }

    /*@Override
    public Topic<TransactionEvent> transactionEvents() {
        return null;
    }*/

    private PersistentEntityRef<TransactionCommand> entityRef(UUID itemId) {
        return registry.refFor(TransactionEntity.class, itemId.toString());
    }
}
