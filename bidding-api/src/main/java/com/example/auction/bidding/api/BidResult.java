package com.example.auction.bidding.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.UUID;

/**
 * The result of placing a bid.
 */
@Value
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
}
