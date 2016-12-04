package com.example.auction.item.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

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

    private ItemEvent() {}

    public abstract UUID getItemId();

    /**
     * Indicates an item has been created or updated.
     */
    @JsonTypeName(value = "item-updated")
    public static final class ItemUpdated extends ItemEvent {
        private final UUID itemId;
        private final UUID creator;
        private final String title;
        private final String description;
        private ItemStatus itemStatus;
        private final String currencyId;

        public ItemUpdated(UUID itemId, UUID creator, String title, String description, ItemStatus itemStatus, String currencyId) {
            this.itemId = itemId;
            this.creator = creator;
            this.title = title;
            this.description = description;
            this.itemStatus = itemStatus;
            this.currencyId = currencyId;
        }

        @Override
        public UUID getItemId() {
            return itemId;
        }

        public UUID getCreator() {
            return creator;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public ItemStatus getItemStatus() {
            return itemStatus;
        }

        public String getCurrencyId() {
            return currencyId;
        }
    }

    /**
     * Indicates an auction has started.
     */
    @JsonTypeName(value = "auction-started")
    public static final class AuctionStarted extends ItemEvent {
        private final UUID itemId;
        private final UUID creator;
        private final int reservePrice;
        private final int increment;
        private final Instant startDate;
        private final Instant endDate;

        public AuctionStarted(UUID itemId, UUID creator, int reservePrice, int increment, Instant startDate, Instant endDate) {
            this.itemId = itemId;
            this.creator = creator;
            this.reservePrice = reservePrice;
            this.increment = increment;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        public UUID getItemId() {
            return itemId;
        }

        public UUID getCreator() {
            return creator;
        }

        public int getReservePrice() {
            return reservePrice;
        }

        public int getIncrement() {
            return increment;
        }

        public Instant getStartDate() {
            return startDate;
        }

        public Instant getEndDate() {
            return endDate;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AuctionStarted that = (AuctionStarted) o;

            if (reservePrice != that.reservePrice) return false;
            if (increment != that.increment) return false;
            if (!itemId.equals(that.itemId)) return false;
            if (!creator.equals(that.creator)) return false;
            if (!startDate.equals(that.startDate)) return false;
            return endDate.equals(that.endDate);

        }

        @Override
        public int hashCode() {
            int result = itemId.hashCode();
            result = 31 * result + creator.hashCode();
            result = 31 * result + reservePrice;
            result = 31 * result + increment;
            result = 31 * result + startDate.hashCode();
            result = 31 * result + endDate.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "AuctionStarted{" +
                    "itemId=" + itemId +
                    ", creator=" + creator +
                    ", reservePrice=" + reservePrice +
                    ", increment=" + increment +
                    ", startDate=" + startDate +
                    ", endDate=" + endDate +
                    '}';
        }
    }

    /**
     * Indicates an auction has finished.
     */
    @JsonTypeName(value = "auction-finished")
    public static final class AuctionFinished extends ItemEvent {
        private final UUID itemId;
        /**
         * Once the auction has finished, the item effectively becomes locked, no further edits, so we can publish it
         * in an event.
         */
        private final Item item;

        public AuctionFinished(UUID itemId, Item item) {
            this.itemId = itemId;
            this.item = item;
        }

        @Override
        public UUID getItemId() {
            return itemId;
        }
    }

    /**
     * Indicates an auction has been cancelled.
     */
    @JsonTypeName(value = "auction-cancelled")
    public static final class AuctionCancelled extends ItemEvent {
        private final UUID itemId;

        public AuctionCancelled(UUID itemId) {
            this.itemId = itemId;
        }

        @Override
        public UUID getItemId() {
            return itemId;
        }
    }
}
