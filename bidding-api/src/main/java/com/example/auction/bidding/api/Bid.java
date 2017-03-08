package com.example.auction.bidding.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * A bid value object.
 */
@Value
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
}
