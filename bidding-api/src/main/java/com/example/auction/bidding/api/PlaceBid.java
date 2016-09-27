package com.example.auction.bidding.api;

import com.fasterxml.jackson.annotation.JsonCreator;

public final class PlaceBid {

    private final int maximumBidPrice;

    @JsonCreator
    public PlaceBid(int maximumBidPrice) {
        this.maximumBidPrice = maximumBidPrice;
    }

    public int getMaximumBidPrice() {
        return maximumBidPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlaceBid placeBid = (PlaceBid) o;

        return maximumBidPrice == placeBid.maximumBidPrice;

    }

    @Override
    public int hashCode() {
        return maximumBidPrice;
    }

    @Override
    public String toString() {
        return "PlaceBid{" +
                "maximumBidPrice=" + maximumBidPrice +
                '}';
    }
}
