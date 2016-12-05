package com.example.auction.item.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

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

    @Value
    class ItemCreated implements PItemEvent {
        PItem item;

        @JsonCreator
        ItemCreated(PItem item) {
            this.item = item;
        }
    }

    @Value
    class ItemUpdated implements PItemEvent {
        UUID id;
        UUID creator;
        PItemData itemDetails;
        PItemStatus itemStatus;

        @JsonCreator
        ItemUpdated(UUID id, UUID creator, PItemData itemDetails, PItemStatus itemStatus) {
            this.id = id;
            this.creator = creator;
            this.itemDetails = itemDetails;
            this.itemStatus = itemStatus;
        }
    }

    @Value
    class AuctionStarted implements PItemEvent {
        UUID itemId;
        Instant startTime;

        @JsonCreator
        AuctionStarted(UUID itemId, Instant startTime) {
            this.itemId = itemId;
            this.startTime = startTime;
        }
    }

    @Value
    class PriceUpdated implements PItemEvent {
        UUID itemId;
        int price;

        @JsonCreator
        PriceUpdated(UUID itemId, int price) {
            this.itemId = itemId;
            this.price = price;
        }
    }

    @Value
    class AuctionFinished implements PItemEvent {
        UUID itemId;
        Optional<UUID> winner;
        int price;

        @JsonCreator
        AuctionFinished(UUID itemId, Optional<UUID> winner, int price) {
            this.itemId = itemId;
            this.winner = winner;
            this.price = price;
        }
    }

}
