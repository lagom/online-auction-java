package com.example.auction.bidding.api;

import java.time.Instant;
import java.util.UUID;

/**
 * A bid value object.
 */
public final class Bid {
    private final UUID bidder;
    private final Instant bidTime;
    private final int price;
}
