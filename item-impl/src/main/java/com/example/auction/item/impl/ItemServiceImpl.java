package com.example.auction.item.impl;

import akka.Done;
import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.javadsl.Flow;
import com.datastax.driver.core.utils.UUIDs;
import com.example.auction.bidding.api.Bid;
import com.example.auction.bidding.api.BidEvent;
import com.example.auction.bidding.api.BiddingService;
import com.example.auction.item.api.*;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.Forbidden;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static com.example.auction.security.ServerSecurity.*;

@Singleton
public class ItemServiceImpl implements ItemService {

    private static final Integer DEFAULT_PAGE_SIZE = 10;

    private final PersistentEntityRegistry registry;
    private final ItemRepository items;

    @Inject
    public ItemServiceImpl(PersistentEntityRegistry registry, BiddingService biddingService, ItemRepository items) {
        this.registry = registry;
        this.items = items;

        registry.register(PItemEntity.class);

        biddingService.bidEvents().subscribe().atLeastOnce(Flow.<BidEvent>create().mapAsync(1, this::handleBidEvent));
    }

    /**
     * TODO: doc this.
     *
     * @param event
     * @return
     */
    private CompletionStage<Done> handleBidEvent(BidEvent event) {
        if (event instanceof BidEvent.BidPlaced) {
            BidEvent.BidPlaced bidPlaced = (BidEvent.BidPlaced) event;
            return entityRef(bidPlaced.getItemId())
                    .ask(new PItemCommand.UpdatePrice(bidPlaced.getBid().getPrice()));
        } else if (event instanceof BidEvent.BiddingFinished) {
            BidEvent.BiddingFinished biddingFinished = (BidEvent.BiddingFinished) event;
            PItemCommand.FinishAuction finishAuction = new PItemCommand.FinishAuction(
                    biddingFinished.getWinningBid().map(Bid::getBidder),
                    biddingFinished.getWinningBid().map(Bid::getPrice).orElse(0));
            return entityRef(biddingFinished.getItemId()).ask(finishAuction);
        } else {
            // Ignore.
            return CompletableFuture.completedFuture(Done.getInstance());
        }
    }


    @Override
    public ServiceCall<Item, Item> createItem() {
        return authenticated(userId -> item -> {
            if (!userId.equals(item.getCreator())) {
                throw new Forbidden("User " + userId + " can't create an item on behalf of " + item.getCreator());
            }
            UUID itemId = UUIDs.timeBased();
            PItem pItem = new PItem(itemId, item.getCreator(), item.getTitle(), item.getDescription(),
                    item.getCurrencyId(), item.getIncrement(), item.getReservePrice(), item.getAuctionDuration());
            return entityRef(itemId).ask(new PItemCommand.CreateItem(pItem)).thenApply(done -> convertItem(pItem));
        });
    }

    @Override
    public ServiceCall<Item, UpdateItemResult> updateItem(UUID itemId) {
        return authenticated(userId -> item -> {
            if (!userId.equals(item.getCreator())) {
                throw new Forbidden("User " + userId + " can't edit an item on behalf of " + item.getCreator());
            }
            PItemCommand.UpdateItem updateItem = new PItemCommand.UpdateItem(
                    item.getId(),
                    item.getCreator(),
                    item.getTitle(),
                    item.getDescription(),
                    item.getCurrencyId(),
                    item.getIncrement(),
                    item.getReservePrice(),
                    item.getAuctionDuration()
            );
            return entityRef(itemId).ask(updateItem).thenApply(
                    pupdate -> new UpdateItemResult(pupdate.getCode())
            );
        });
    }

    @Override
    public ServiceCall<NotUsed, Done> startAuction(UUID id) {
        return authenticated(userId -> req ->
                entityRef(id).ask(new PItemCommand.StartAuction(userId))
        );
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
                item.getStatus().toItemStatus(), item.getAuctionDuration(), item.getAuctionStart(),
                item.getAuctionEnd(), item.getAuctionWinner());
    }

    @Override
    public ServiceCall<NotUsed, PaginatedSequence<ItemSummary>> getItemsForUser(
            UUID id, ItemStatus status, Optional<Integer> pageNo, Optional<Integer> pageSize) {
        return req ->
                items.getItemsForUser(id, status, pageNo.orElse(0), pageSize.orElse(DEFAULT_PAGE_SIZE));
    }

    @Override
    public Topic<ItemEvent> itemEvents() {
        return TopicProducer.taggedStreamWithOffset(PItemEvent.TAG.allTags(), (tag, offset) -> {
            return registry.eventStream(tag, offset)
                    .filter(this::filterEvent)
                    .mapAsync(1, eventAndOffset ->
                            convertEvent(eventAndOffset.first()).thenApply(event ->
                                    Pair.create(event, eventAndOffset.second())));
        });
    }

    private boolean filterEvent(Pair<PItemEvent, Offset> event) {
        return event.first() instanceof PItemEvent.AuctionStarted;
    }

    private CompletionStage<ItemEvent> convertEvent(PItemEvent event) {
        if (event instanceof PItemEvent.AuctionStarted) {
            return entityRef(((PItemEvent.AuctionStarted) event).getItemId())
                    .ask(PItemCommand.GetItem.INSTANCE)
                    .thenApply(maybeItem -> {
                        PItem item = maybeItem.get();
                        return new ItemEvent.AuctionStarted(item.getId(), item.getCreator(), item.getReservePrice(),
                                item.getIncrement(), item.getAuctionStart().get(), item.getAuctionEnd().get());
                    });
        } else {
            throw new IllegalArgumentException("Converting non public event");
        }
    }

    private PersistentEntityRef<PItemCommand> entityRef(UUID itemId) {
        return registry.refFor(PItemEntity.class, itemId.toString());
    }
}
