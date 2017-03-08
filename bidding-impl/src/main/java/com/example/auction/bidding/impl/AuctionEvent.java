package com.example.auction.bidding.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.UUID;

/**
 * A persisted auction event.
 */
public interface AuctionEvent extends Jsonable, AggregateEvent<AuctionEvent> {

    int NUM_SHARDS = 4;
    AggregateEventShards<AuctionEvent> TAG = AggregateEventTag.sharded(AuctionEvent.class, NUM_SHARDS);

    @Override
    default AggregateEventTagger<AuctionEvent> aggregateTag() {
        return TAG;
    }

    /**
     * The auction started.
     */
    @Value
    final class AuctionStarted implements AuctionEvent {

        private static final long serialVersionUID = 1L;

        /**
         * The item that the auction started on.
         */
        private final UUID itemId;
        /**
         * The auction details.
         */
        private final Auction auction;

        @JsonCreator
        public AuctionStarted(UUID itemId, Auction auction) {
            this.itemId = itemId;
            this.auction = auction;
        }
    }

    /**
     * A bid was placed.
     */
    @Value
    final class BidPlaced implements AuctionEvent {

        private static final long serialVersionUID = 1L;

        /**
         * The item that the bid was placed on.
         */
        private final UUID itemId;
        /**
         * The bid.
         */
        private final Bid bid;

        @JsonCreator
        public BidPlaced(UUID itemId, Bid bid) {
            this.itemId = itemId;
            this.bid = bid;
        }
    }

    /**
     * Bidding finished.
     */
    @Value
    final class BiddingFinished implements AuctionEvent {

        private static final long serialVersionUID = 1L;

        /**
         * The item that bidding finished for.
         */
        private final UUID itemId;

        @JsonCreator
        public BiddingFinished(UUID itemId) {
            this.itemId = itemId;
        }
    }

    /**
     * The auction was cancelled.
     */
    @Value
    final class AuctionCancelled implements AuctionEvent {

        private static final long serialVersionUID = 1L;

        /**
         * The item that the auction was cancelled for.
         */
        private final UUID itemId;

        @JsonCreator
        public AuctionCancelled(UUID itemId) {
            this.itemId = itemId;
        }
    }

}
