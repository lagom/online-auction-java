package com.example.auction.transaction.impl;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Flow;
import com.example.auction.transaction.api.TransactionInfo;
import com.example.auction.transaction.impl.TransactionCommand.*;
import com.example.auction.item.api.Item;
import com.example.auction.item.api.ItemEvent;
import com.example.auction.item.api.ItemService;
import com.example.auction.transaction.api.DeliveryInfo;
import com.example.auction.transaction.api.TransactionService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.example.auction.security.ServerSecurity.authenticated;

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
                // If an auction doesn't have a winner, then we can't start a transaction
                if(auctionFinished.getItem().getAuctionWinner().isPresent()) {
                    Item item = auctionFinished.getItem();
                    Transaction transaction = new Transaction(item.getId(), item.getCreator(),
                            item.getAuctionWinner().get(), item.getItemData(), item.getPrice());
                    return entityRef(auctionFinished.getItemId()).ask(new StartTransaction(transaction));
                }
                else
                    return CompletableFuture.completedFuture(Done.getInstance());
            } else
                return CompletableFuture.completedFuture(Done.getInstance());
        }));
    }

    /*@Override
    public Topic<TransactionEvent> transactionEvents() {
        return null;
    }*/

    @Override
    public ServiceCall<DeliveryInfo, Done> submitDeliveryDetails(UUID itemId) {
        return authenticated(userId -> deliveryInfo -> {
            SubmitDeliveryDetails submit = new SubmitDeliveryDetails(userId, TransactionMappers.fromApi(deliveryInfo));
            return entityRef(itemId).ask(submit);
        });
    }

    @Override
    public ServiceCall<NotUsed, TransactionInfo> getTransaction(UUID itemId) {
        return authenticated(userId -> request -> {
            GetTransaction get = new GetTransaction(userId);
            return entityRef(itemId)
                    .ask(get)
                    .thenApply(transaction -> {
                        TransactionInfo transactionInfo = TransactionMappers.toApi((TransactionState) transaction);
                        return transactionInfo;
                    });
        });

    }

    private PersistentEntityRef<TransactionCommand> entityRef(UUID itemId) {
        return registry.refFor(TransactionEntity.class, itemId.toString());
    }
}
