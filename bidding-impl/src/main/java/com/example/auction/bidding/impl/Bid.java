package com.example.auction.bidding.impl;

import java.time.Instant;
import java.util.UUID;

/**
 * A bid.
 */
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

    public UUID getBidder() {
        return bidder;
    }

    public Instant getBidTime() {
        return bidTime;
    }

    public int getBidPrice() {
        return bidPrice;
    }

    public int getMaximumBid() {
        return maximumBid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bid bid1 = (Bid) o;

        if (bidPrice != bid1.bidPrice) return false;
        if (maximumBid != bid1.maximumBid) return false;
        if (!bidder.equals(bid1.bidder)) return false;
        return bidTime.equals(bid1.bidTime);

    }

    @Override
    public int hashCode() {
        int result = bidder.hashCode();
        result = 31 * result + bidTime.hashCode();
        result = 31 * result + bidPrice;
        result = 31 * result + maximumBid;
        return result;
    }

    @Override
    public String toString() {
        return "Bid{" +
                "bidder=" + bidder +
                ", bidTime=" + bidTime +
                ", bidPrice=" + bidPrice +
                ", maximumBid=" + maximumBid +
                '}';
    }
}
