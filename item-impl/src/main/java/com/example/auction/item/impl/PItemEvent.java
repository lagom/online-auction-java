package com.example.auction.item.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import org.pcollections.PSequence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PItemEvent extends AggregateEvent<PItemEvent>, Jsonable {

    int NUM_SHARDS = 4;
    PSequence<AggregateEventTag<PItemEvent>> TAGS = AggregateEventTag.shards(PItemEvent.class, NUM_SHARDS);

    final class ItemCreated implements PItemEvent {
        private final PItem item;

        @JsonCreator
        public ItemCreated(PItem item) {
            this.item = item;
        }

        @Override
        public AggregateEventTag<PItemEvent> aggregateTag() {
            return AggregateEventTag.shard(PItemEvent.class, NUM_SHARDS, item.getId().toString());
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
    }

    final class AuctionStarted implements PItemEvent {
        private final UUID itemId;
        private final Instant startTime;

        @JsonCreator
        public AuctionStarted(UUID itemId, Instant startTime) {
            this.itemId = itemId;
            this.startTime = startTime;
        }

        @Override
        public AggregateEventTag<PItemEvent> aggregateTag() {
            return AggregateEventTag.shard(PItemEvent.class, NUM_SHARDS, itemId.toString());
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
    }

    final class PriceUpdated implements PItemEvent {
        private final UUID itemId;
        private final int price;

        @JsonCreator
        public PriceUpdated(UUID itemId, int price) {
            this.itemId = itemId;
            this.price = price;
        }

        @Override
        public AggregateEventTag<PItemEvent> aggregateTag() {
            return AggregateEventTag.shard(PItemEvent.class, NUM_SHARDS, itemId.toString());
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

        @Override
        public AggregateEventTag<PItemEvent> aggregateTag() {
            return AggregateEventTag.shard(PItemEvent.class, NUM_SHARDS, itemId.toString());
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
