package com.example.auction.item.impl;

import akka.Done;
import akka.NotUsed;
import com.example.auction.item.api.Item;
import com.example.auction.item.api.ItemService;
import com.example.auction.item.api.ItemStatus;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import org.pcollections.PSequence;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class ItemServiceImpl implements ItemService {

    private final PersistentEntityRegistry registry;

    @Inject
    public ItemServiceImpl(PersistentEntityRegistry registry) {
        this.registry = registry;

        registry.register(PItemEntity.class);
    }

    @Override
    public ServiceCall<Item, Item> createItem() {
        return item -> {
            UUID itemId = UUID.randomUUID();
            PItem pItem = new PItem(itemId, item.getCreator(), item.getTitle(), item.getDescription(),
                    item.getCurrencyId(), item.getIncrement(), item.getReservePrice(), item.getAuctionDuration());
            return entityRef(itemId).ask(new PItemCommand.CreateItem(pItem)).thenApply(done -> convertItem(pItem));
        };
    }

    @Override
    public ServiceCall<Item, Done> updateItem(UUID id) {
        return item -> {
            // todo implement
            return null;
        };
    }

    @Override
    public ServiceCall<NotUsed, Done> startAuction(UUID id) {
        return req -> entityRef(id).ask(PItemCommand.StartAuction.INSTANCE);
    }

    @Override
    public ServiceCall<NotUsed, Item> getItem(UUID id) {
        return req -> entityRef(id).ask(PItemCommand.GetItem.INSTANCE).thenApply(maybeItem -> {
            if (maybeItem.isPresent()) {
                return convertItem(maybeItem.get());
            } else {
                throw new NotFound("Item " + id + " not found");
            }
        });
    }

    private Item convertItem(PItem item) {
        return new Item(item.getId(), item.getCreator(), item.getTitle(), item.getDescription(),
                item.getCurrencyId(), item.getIncrement(), item.getReservePrice(), item.getPrice(),
                convertStatus(item.getStatus()), item.getAuctionDuration(), item.getAuctionStart(),
                item.getAuctionEnd(), item.getAuctionWinner());
    }

    private ItemStatus convertStatus(PItemStatus status) {
        switch (status) {
            case NOT_CREATED:
                throw new IllegalStateException("Publicly exposed item can't be not created");
            case CREATED:
                return ItemStatus.CREATED;
            case AUCTION:
                return ItemStatus.AUCTION;
            case COMPLETED:
                return ItemStatus.COMPLETED;
            case CANCELLED:
                return ItemStatus.CANCELLED;
            default:
                throw new IllegalArgumentException("Unknown status " + status);
        }
    }

    @Override
    public ServiceCall<NotUsed, PSequence<Item>> getItemsForUser(UUID id, Optional<Integer> pageNo, Optional<Integer> pageSize) {
        return req -> {
            // todo implement
            return null;
        };
    }

    private PersistentEntityRef<PItemCommand> entityRef(UUID itemId) {
        return registry.refFor(PItemEntity.class, itemId.toString());
    }
}
