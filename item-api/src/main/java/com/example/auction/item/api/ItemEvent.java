package com.example.auction.item.api;

import java.time.Instant;
import java.util.UUID;

/**
 * Events pertaining to times.
 */
public abstract class ItemEvent {

    private ItemEvent() {}

    public abstract UUID getItemId();

    /**
     * Indicates an item has been created or updated.
     */
    public static final class ItemUpdated extends ItemEvent {
        private final UUID itemId;
        private final UUID creator;
        private final String title;
        private final String description;
        private final UUID categoryId;
        private final String currencyId;
    }

    /**
     * Indicates an auction has started.
     */
    public static final class AuctionStarted extends ItemEvent {
        private final UUID itemId;
        private final UUID creator;
        private final int reservePrice;
        private final Instant startDate;
        private final Instant endDate;
    }

    /**
     * Indicates an auction has finished.
     */
    public static final class AuctionFinished extends ItemEvent {
        private final UUID itemId;
        /**
         * Once the auction has finished, the item effectively becomes locked, no further edits, so we can publish it
         * in an event.
         */
        private final Item item;
    }

    /**
     * Indidates an auction has been cancelled.
     */
    public static final class AuctionCancelled extends ItemEvent {
        private final UUID itemId;
    }
}
