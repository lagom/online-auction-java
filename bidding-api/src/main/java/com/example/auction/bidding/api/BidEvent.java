package com.example.auction.bidding.api;

import java.time.Instant;
import java.util.UUID;

public abstract class BidEvent {

    private BidEvent() {}

    public static final class BidPlaced extends BidEvent {

        private final UUID itemId;
        private final Bid bid;

    }

    public static final class Bid


}
