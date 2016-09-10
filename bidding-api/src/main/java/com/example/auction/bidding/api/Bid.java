package com.example.auction.bidding.api;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.Instant;
import java.util.UUID;

/**
 * A bid value object.
 */
public final class Bid {
    /**
     * The user that placed the bid.
     */
    private final UUID bidder;
    /**
     * The time that the bid was placed.
     */
    private final Instant bidTime;
    /**
     * The bid price.
     */
    private final int price;

    @JsonCreator
    public Bid(UUID bidder, Instant bidTime, int price) {
        this.bidder = bidder;
        this.bidTime = bidTime;
        this.price = price;
    }

    public UUID getBidder() {
        return bidder;
    }

    public Instant getBidTime() {
        return bidTime;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bid bid = (Bid) o;

        if (price != bid.price) return false;
        if (!bidder.equals(bid.bidder)) return false;
        return bidTime != null ? bidTime.equals(bid.bidTime) : bid.bidTime == null;

    }

    @Override
    public int hashCode() {
        int result = bidder.hashCode();
        result = 31 * result + (bidTime != null ? bidTime.hashCode() : 0);
        result = 31 * result + price;
        return result;
    }

    @Override
    public String toString() {
        return "Bid{" +
                "bidder=" + bidder +
                ", bidTime=" + bidTime +
                ", price=" + price +
                '}';
    }
}
