package com.example.auction.item.api;

/**
 * The status of an item.
 */
public enum ItemStatus {
    /**
     * The item has been created, but the auction is yet to start.
     */
    CREATED,
    /**
     * The item is under auction.
     */
    AUCTION,
    /**
     * The items auction has been completed.
     */
    COMPLETED,
    /**
     * The auction was cancelled.
     */
    CANCELLED
}
