package com.example.auction.item.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import org.pcollections.PSequence;

import java.util.UUID;

public interface PItemEvent extends AggregateEvent<PItemEvent>, Jsonable {

    int NUM_SHARDS = 20;
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

        @JsonCreator
        public AuctionStarted(UUID itemId) {
            this.itemId = itemId;
        }

        @Override
        public AggregateEventTag<PItemEvent> aggregateTag() {
            return AggregateEventTag.shard(PItemEvent.class, NUM_SHARDS, itemId.toString());
        }

        public UUID getItemId() {
            return itemId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AuctionStarted that = (AuctionStarted) o;

            return itemId.equals(that.itemId);

        }

        @Override
        public int hashCode() {
            return itemId.hashCode();
        }
    }

}
