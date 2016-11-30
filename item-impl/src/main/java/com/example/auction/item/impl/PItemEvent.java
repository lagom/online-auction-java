package com.example.auction.item.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PItemEvent extends AggregateEvent<PItemEvent>, Jsonable {

    int NUM_SHARDS = 4;
    AggregateEventShards<PItemEvent> TAG = AggregateEventTag.sharded(PItemEvent.class, NUM_SHARDS);

    @Override
    default AggregateEventTagger<PItemEvent> aggregateTag() {
        return TAG;
    }

    final class ItemCreated implements PItemEvent {
        private final PItem item;

        @JsonCreator
        public ItemCreated(PItem item) {
            this.item = item;
        }

        public PItem getItem() {
            return item;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ItemCreated that = (ItemCreated) o;

            return item.equals(that.item);

        }

        @Override
        public int hashCode() {
            return item.hashCode();
        }

        @Override
        public String toString() {
            return "ItemCreated{" +
                    "item=" + item +
                    '}';
        }
    }

    final class ItemUpdated implements PItemEvent {
        private final UUID id;
        private final UUID creator;
        private final PItemDetails itemDetails;


        @JsonCreator
        private ItemUpdated(UUID id, UUID creator, PItemDetails itemDetails) {
            this.id = id;
            this.creator = creator;
            this.itemDetails = itemDetails;
        }


        public static ItemUpdated from(UUID id, UUID creatorId, PItemDetails itemDetails) {
            return new ItemUpdated(id, creatorId, itemDetails);
        }

        public UUID getId() {
            return id;
        }

        public UUID getCreator() {
            return creator;
        }

        public PItemDetails getItemDetails() {
            return itemDetails;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ItemUpdated that = (ItemUpdated) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (creator != null ? !creator.equals(that.creator) : that.creator != null) return false;
            return itemDetails != null ? itemDetails.equals(that.itemDetails) : that.itemDetails == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (creator != null ? creator.hashCode() : 0);
            result = 31 * result + (itemDetails != null ? itemDetails.hashCode() : 0);
            return result;
        }
    }


    final class AuctionStarted implements PItemEvent {
        private final UUID itemId;
        private final Instant startTime;

        @JsonCreator
        public AuctionStarted(UUID itemId, Instant startTime) {
            this.itemId = itemId;
            this.startTime = startTime;
        }

        public UUID getItemId() {
            return itemId;
        }

        public Instant getStartTime() {
            return startTime;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AuctionStarted that = (AuctionStarted) o;

            if (!itemId.equals(that.itemId)) return false;
            return startTime.equals(that.startTime);

        }

        @Override
        public int hashCode() {
            int result = itemId.hashCode();
            result = 31 * result + startTime.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "AuctionStarted{" +
                    "itemId=" + itemId +
                    ", startTime=" + startTime +
                    '}';
        }
    }

    final class PriceUpdated implements PItemEvent {
        private final UUID itemId;
        private final int price;

        @JsonCreator
        public PriceUpdated(UUID itemId, int price) {
            this.itemId = itemId;
            this.price = price;
        }

        public UUID getItemId() {
            return itemId;
        }

        public int getPrice() {
            return price;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PriceUpdated that = (PriceUpdated) o;

            if (price != that.price) return false;
            return itemId.equals(that.itemId);

        }

        @Override
        public int hashCode() {
            int result = itemId.hashCode();
            result = 31 * result + price;
            return result;
        }
    }

    final class AuctionFinished implements PItemEvent {
        private final UUID itemId;
        private final Optional<UUID> winner;
        private final int price;

        @JsonCreator
        public AuctionFinished(UUID itemId, Optional<UUID> winner, int price) {
            this.itemId = itemId;
            this.winner = winner;
            this.price = price;
        }

        public UUID getItemId() {
            return itemId;
        }

        public Optional<UUID> getWinner() {
            return winner;
        }

        public int getPrice() {
            return price;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AuctionFinished that = (AuctionFinished) o;

            if (price != that.price) return false;
            if (!itemId.equals(that.itemId)) return false;
            return winner.equals(that.winner);

        }

        @Override
        public int hashCode() {
            int result = itemId.hashCode();
            result = 31 * result + winner.hashCode();
            result = 31 * result + price;
            return result;
        }
    }


}
