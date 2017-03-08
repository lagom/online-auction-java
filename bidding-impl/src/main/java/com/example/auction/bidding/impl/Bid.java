package com.example.auction.bidding.impl;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * A bid.
 */
@Value
public final class Bid {
    /**
     * The bidder.
     */
    private final UUID bidder;
    /**
     * The time the bid was placed.
     */
    private final Instant bidTime;
    /**
     * The bid price.
     */
    private final int bidPrice;
    /**
     * The maximum the bidder is willing to bid.
     */
    private final int maximumBid;

    public Bid(UUID bidder, Instant bidTime, int bidPrice, int maximumBid) {
        this.bidder = bidder;
        this.bidTime = bidTime;
        this.bidPrice = bidPrice;
        this.maximumBid = maximumBid;
    }
}
