package com.example.auction.bidding.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * An auction.
 */
@Value
public final class Auction {
    /**
     * The item under auction.
     */
    private final UUID itemId;
    /**
     * The user that created the item.
     */
    private final UUID creator;
    /**
     * The reserve price of the auction.
     */
    private final int reservePrice;
    /**
     * The minimum increment between bids.
     */
    private final int increment;
    /**
     * The time the auction started.
     */
    private final Instant startTime;
    /**
     * The time the auction will end.
     */
    private final Instant endTime;

    @JsonCreator
    public Auction(UUID itemId, UUID creator, int reservePrice, int increment, Instant startTime, Instant endTime) {
        this.itemId = itemId;
        this.creator = creator;
        this.reservePrice = reservePrice;
        this.increment = increment;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
