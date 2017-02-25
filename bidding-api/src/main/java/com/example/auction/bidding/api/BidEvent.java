package com.example.auction.bidding.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * A bid event.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
                @JsonSubTypes.Type(BidEvent.BidPlaced.class),
                @JsonSubTypes.Type(BidEvent.BiddingFinished.class)
})
public interface BidEvent {

    UUID getItemId();
    /**
     * A bid was placed.
     */
    @JsonTypeName("bid-placed")
    @Value
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
    }

    /**
     * Bidding finished.
     */
    @JsonTypeName("bidding-finished")
    @Value
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
    }

}
