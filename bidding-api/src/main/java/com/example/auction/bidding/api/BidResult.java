package com.example.auction.bidding.api;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.UUID;

/**
 * The result of placing a bid.
 */
public final class BidResult {
    /**
     * The current bid price.
     */
    private final int currentPrice;
    /**
     * The status of the result.
     */
    private final BidResultStatus status;
    /**
     * The current winning bidder.
     */
    private final UUID currentBidder;

    @JsonCreator
    public BidResult(int currentPrice, BidResultStatus status, UUID currentBidder) {
        this.currentPrice = currentPrice;
        this.status = status;
        this.currentBidder = currentBidder;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public BidResultStatus getStatus() {
        return status;
    }

    public UUID getCurrentBidder() {
        return currentBidder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BidResult bidResult = (BidResult) o;

        if (currentPrice != bidResult.currentPrice) return false;
        if (status != bidResult.status) return false;
        return currentBidder != null ? currentBidder.equals(bidResult.currentBidder) : bidResult.currentBidder == null;

    }

    @Override
    public int hashCode() {
        int result = currentPrice;
        result = 31 * result + status.hashCode();
        result = 31 * result + (currentBidder != null ? currentBidder.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BidResult{" +
                "currentPrice=" + currentPrice +
                ", status=" + status +
                ", currentBidder=" + currentBidder +
                '}';
    }
}
