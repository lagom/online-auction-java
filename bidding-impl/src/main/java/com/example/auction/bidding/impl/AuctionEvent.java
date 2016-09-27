package com.example.auction.bidding.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import org.pcollections.PSequence;

import java.util.UUID;

/**
 * A persisted auction event.
 */
public interface AuctionEvent extends Jsonable, AggregateEvent<AuctionEvent> {

    int NUM_SHARDS = 4;
    PSequence<AggregateEventTag<AuctionEvent>> TAGS = AggregateEventTag.shards(AuctionEvent.class, NUM_SHARDS);

    /**
     * The auction started.
     */
    final class AuctionStarted implements AuctionEvent {
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

        public UUID getItemId() {
            return itemId;
        }

        public Auction getAuction() {
            return auction;
        }

        @Override
        public AggregateEventTag<AuctionEvent> aggregateTag() {
            return AggregateEventTag.shard(AuctionEvent.class, NUM_SHARDS, itemId.toString());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AuctionStarted that = (AuctionStarted) o;

            if (!itemId.equals(that.itemId)) return false;
            return auction.equals(that.auction);

        }

        @Override
        public int hashCode() {
            int result = itemId.hashCode();
            result = 31 * result + auction.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "AuctionStarted{" +
                    "itemId=" + itemId +
                    ", auction=" + auction +
                    '}';
        }
    }

    /**
     * A bid was placed.
     */
    final class BidPlaced implements AuctionEvent {
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

        public UUID getItemId() {
            return itemId;
        }

        public Bid getBid() {
            return bid;
        }

        @Override
        public AggregateEventTag<AuctionEvent> aggregateTag() {
            return AggregateEventTag.shard(AuctionEvent.class, NUM_SHARDS, itemId.toString());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BidPlaced bidPlaced = (BidPlaced) o;

            if (!itemId.equals(bidPlaced.itemId)) return false;
            return bid.equals(bidPlaced.bid);

        }

        @Override
        public int hashCode() {
            int result = itemId.hashCode();
            result = 31 * result + bid.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "BidPlaced{" +
                    "itemId=" + itemId +
                    ", bid=" + bid +
                    '}';
        }
    }

    /**
     * Bidding finished.
     */
    final class BiddingFinished implements AuctionEvent {
        /**
         * The item that bidding finished for.
         */
        private final UUID itemId;

        @JsonCreator
        public BiddingFinished(UUID itemId) {
            this.itemId = itemId;
        }

        public UUID getItemId() {
            return itemId;
        }

        @Override
        public AggregateEventTag<AuctionEvent> aggregateTag() {
            return AggregateEventTag.shard(AuctionEvent.class, NUM_SHARDS, itemId.toString());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BiddingFinished that = (BiddingFinished) o;

            return itemId.equals(that.itemId);

        }

        @Override
        public int hashCode() {
            return itemId.hashCode();
        }

        @Override
        public String toString() {
            return "BiddingFinished{" +
                    "itemId=" + itemId +
                    '}';
        }
    }

    /**
     * The auction was cancelled.
     */
    final class AuctionCancelled implements AuctionEvent {
        /**
         * The item that the auction was cancelled for.
         */
        private final UUID itemId;

        @JsonCreator
        public AuctionCancelled(UUID itemId) {
            this.itemId = itemId;
        }

        public UUID getItemId() {
            return itemId;
        }

        @Override
        public AggregateEventTag<AuctionEvent> aggregateTag() {
            return AggregateEventTag.shard(AuctionEvent.class, NUM_SHARDS, itemId.toString());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AuctionCancelled that = (AuctionCancelled) o;

            return itemId.equals(that.itemId);

        }

        @Override
        public int hashCode() {
            return itemId.hashCode();
        }

        @Override
        public String toString() {
            return "AuctionCancelled{" +
                    "itemId=" + itemId +
                    '}';
        }
    }

}
