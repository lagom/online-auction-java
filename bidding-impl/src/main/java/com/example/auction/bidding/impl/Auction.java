package com.example.auction.bidding.impl;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.Instant;
import java.util.UUID;

/**
 * An auction.
 */
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

    public UUID getItemId() {
        return itemId;
    }

    public UUID getCreator() {
        return creator;
    }

    public int getReservePrice() {
        return reservePrice;
    }

    public int getIncrement() {
        return increment;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Auction auction = (Auction) o;

        if (reservePrice != auction.reservePrice) return false;
        if (increment != auction.increment) return false;
        if (!itemId.equals(auction.itemId)) return false;
        if (!creator.equals(auction.creator)) return false;
        if (!startTime.equals(auction.startTime)) return false;
        return endTime.equals(auction.endTime);

    }

    @Override
    public int hashCode() {
        int result = itemId.hashCode();
        result = 31 * result + creator.hashCode();
        result = 31 * result + reservePrice;
        result = 31 * result + increment;
        result = 31 * result + startTime.hashCode();
        result = 31 * result + endTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Auction{" +
                "itemId=" + itemId +
                ", creator=" + creator +
                ", reservePrice=" + reservePrice +
                ", increment=" + increment +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

}
