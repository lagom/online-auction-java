package com.example.auction.search.impl;

import akka.stream.javadsl.Flow;
import com.example.auction.bidding.api.BidEvent;
import com.example.auction.bidding.api.BiddingService;
import com.example.auction.item.api.ItemEvent;
import com.example.auction.item.api.ItemService;
import com.example.auction.search.IndexedStore;
import com.example.elasticsearch.IndexedItem;
import com.lightbend.lagom.javadsl.api.broker.Topic;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class BrokerEventConsumer {

    private IndexedStore indexedStore;
    private ItemService itemService;
    private BiddingService biddingService;

    @Inject
    public BrokerEventConsumer(IndexedStore indexedStore, ItemService itemService, BiddingService biddingService) {
        this.indexedStore = indexedStore;
        this.itemService = itemService;
        this.biddingService = biddingService;

        // TODO: use ES' _bulk API
        Topic<ItemEvent> itemEventTopic = itemService.itemEvents();
        Topic<BidEvent> bidEventTopic = biddingService.bidEvents();
        itemEventTopic.subscribe().atLeastOnce(Flow.<ItemEvent>create().map(this::toDocument).mapAsync(1, indexedStore::store));
        bidEventTopic.subscribe().atLeastOnce(Flow.<BidEvent>create().map(this::toDocument).mapAsync(1, indexedStore::store));
    }

    private Optional<IndexedItem> toDocument(ItemEvent event) {
        if (event instanceof ItemEvent.AuctionStarted) {
            ItemEvent.AuctionStarted started = (ItemEvent.AuctionStarted) event;
            return Optional.of(IndexedItem.forAuctionStart(started.getItemId(), started.getStartDate(), started.getEndDate()));
        } else if (event instanceof ItemEvent.AuctionFinished) {
            ItemEvent.AuctionFinished finish = (ItemEvent.AuctionFinished) event;
            return Optional.of(IndexedItem.forAuctionFinish(finish.getItemId(), finish.getItem()));
        } else if (event instanceof ItemEvent.ItemUpdated) {
            ItemEvent.ItemUpdated details = (ItemEvent.ItemUpdated) event;
            return Optional.of(IndexedItem.forItemDetails(
                    details.getItemId(),
                    details.getCreator(),
                    details.getTitle(),
                    details.getDescription(),
                    details.getItemStatus(),
                    details.getCurrencyId()));
        } else {
            return Optional.empty();
        }
    }

    private Optional<IndexedItem> toDocument(BidEvent event) {
        if (event instanceof BidEvent.BidPlaced) {
            BidEvent.BidPlaced bid = (BidEvent.BidPlaced) event;
            return Optional.of(IndexedItem.forPrice(bid.getItemId(), bid.getBid().getPrice()));
        } else if (event instanceof BidEvent.BiddingFinished) {
            BidEvent.BiddingFinished bid = (BidEvent.BiddingFinished) event;
            return Optional.of(bid.getWinningBid()
                    .map(winning -> IndexedItem.forWinningBid(bid.getItemId(), winning.getPrice(), winning.getBidder()))
                    .orElse(IndexedItem.forPrice(bid.getItemId(), 0)));
        } else {
            return Optional.empty();
        }
    }
}