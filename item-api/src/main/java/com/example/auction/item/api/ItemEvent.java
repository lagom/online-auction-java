package com.example.auction.item.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Events pertaining to items.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(ItemEvent.ItemUpdated.class),
        @JsonSubTypes.Type(ItemEvent.AuctionStarted.class),
        @JsonSubTypes.Type(ItemEvent.AuctionFinished.class),
        @JsonSubTypes.Type(ItemEvent.AuctionCancelled.class)
})
public abstract class ItemEvent {

    private ItemEvent() {
    }

    public abstract UUID getItemId();

    /**
     * Indicates an item has been created or updated.
     */
    @JsonTypeName(value = "item-updated")
    @Value
    public static final class ItemUpdated extends ItemEvent {
        UUID itemId;
        UUID creator;
        String title;
        String description;
        ItemStatus itemStatus;
        String currencyId;

        // TODO: review what's published
        public ItemUpdated(UUID itemId, UUID creator, String title, String description, ItemStatus itemStatus, String currencyId) {
            this.itemId = itemId;
            this.creator = creator;
            this.title = title;
            this.description = description;
            this.itemStatus = itemStatus;
            this.currencyId = currencyId;
        }
    }


    /**
     * Indicates an auction has started.
     */
    @JsonTypeName(value = "auction-started")
    @Value
    public static final class AuctionStarted extends ItemEvent {
        UUID itemId;
        UUID creator;
        int reservePrice;
        int increment;
        Instant startDate;
        Instant endDate;

        public AuctionStarted(UUID itemId, UUID creator, int reservePrice, int increment, Instant startDate, Instant endDate) {
            this.itemId = itemId;
            this.creator = creator;
            this.reservePrice = reservePrice;
            this.increment = increment;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    /**
     * Indicates an auction has finished.
     */
    @JsonTypeName(value = "auction-finished")
    @Value
    public static final class AuctionFinished extends ItemEvent {
        UUID itemId;
        /**
         * Once the auction has finished, the item effectively becomes locked, no further edits, so we can publish it
         * in an event.
         */
        Item item;

        public AuctionFinished(UUID itemId, Item item) {
            this.itemId = itemId;
            this.item = item;
        }
    }

    /**
     * Indicates an auction has been cancelled.
     */
    @JsonTypeName(value = "auction-cancelled")
    @Value
    public static final class AuctionCancelled extends ItemEvent {
        UUID itemId;

        public AuctionCancelled(UUID itemId) {
            this.itemId = itemId;
        }

    }
}
