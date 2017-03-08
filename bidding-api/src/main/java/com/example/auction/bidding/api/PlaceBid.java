package com.example.auction.bidding.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

/**
 * A request to place a bid.
 */
@Value
public final class PlaceBid {

    /**
     * The maximum bid price.
     */
    private final int maximumBidPrice;

    @JsonCreator
    public PlaceBid(int maximumBidPrice) {
        this.maximumBidPrice = maximumBidPrice;
    }
}
