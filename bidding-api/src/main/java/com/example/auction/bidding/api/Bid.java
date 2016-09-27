package com.example.auction.bidding.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    /**
     * The maximum bid price;
     */
    private final int maximumPrice;

    @JsonCreator
    // parameter annotations needed until https://github.com/lagom/lagom/issues/172 is fixed.
    public Bid(@JsonProperty("bidder") UUID bidder, @JsonProperty("bidTime") Instant bidTime,
            @JsonProperty("price") int price, @JsonProperty("maximumPrice") int maximumPrice) {
        this.bidder = bidder;
        this.bidTime = bidTime;
        this.price = price;
        this.maximumPrice = maximumPrice;
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

    public int getMaximumPrice() {
        return maximumPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bid bid = (Bid) o;

        if (price != bid.price) return false;
        if (maximumPrice != bid.maximumPrice) return false;
        if (!bidder.equals(bid.bidder)) return false;
        return bidTime.equals(bid.bidTime);

    }

    @Override
    public int hashCode() {
        int result = bidder.hashCode();
        result = 31 * result + bidTime.hashCode();
        result = 31 * result + price;
        result = 31 * result + maximumPrice;
        return result;
    }

    @Override
    public String toString() {
        return "Bid{" +
                "bidder=" + bidder +
                ", bidTime=" + bidTime +
                ", price=" + price +
                ", maximumPrice=" + maximumPrice +
                '}';
    }
}
