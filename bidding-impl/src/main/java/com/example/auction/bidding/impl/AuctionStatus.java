package com.example.auction.bidding.impl;

/**
 * Auction status.
 */
public enum AuctionStatus {
    /**
     * The auction hasn't started yet (or doesn't exist).
     */
    NOT_STARTED,
    /**
     * The item is under auction.
     */
    UNDER_AUCTION,
    /**
     * The auction is complete.
     */
    COMPLETE,
    /**
     * The auction is cancelled.
     */
    CANCELLED
}
