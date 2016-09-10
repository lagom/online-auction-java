package com.example.auction.bidding.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * A bid event.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface BidEvent {

    /**
     * A bid was placed.
     */
    @JsonTypeName("bid-placed")
    final class BidPlaced implements BidEvent {

        /**
         * The item the bid was placed on.
         */
        private final UUID itemId;
        /**
         * The bid itself.
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
    @JsonTypeName("bidding-finished")
    final class BiddingFinished implements BidEvent {

        /**
         * The item that finished bidding.
         */
        private final UUID itemId;
        /**
         * The winning bid, if there was one.
         */
        private final Optional<Bid> winningBid;

        @JsonCreator
        public BiddingFinished(UUID itemId, Optional<Bid> winningBid) {
            this.itemId = itemId;
            this.winningBid = winningBid;
        }

        public UUID getItemId() {
            return itemId;
        }

        public Optional<Bid> getWinningBid() {
            return winningBid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BiddingFinished that = (BiddingFinished) o;

            if (!itemId.equals(that.itemId)) return false;
            return winningBid.equals(that.winningBid);

        }

        @Override
        public int hashCode() {
            int result = itemId.hashCode();
            result = 31 * result + winningBid.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "BiddingFinished{" +
                    "itemId=" + itemId +
                    ", winningBid=" + winningBid +
                    '}';
        }
    }

}
