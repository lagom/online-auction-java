package com.example.auction.transaction.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.UUID;

public interface TransactionEvent extends AggregateEvent<TransactionEvent>, Jsonable {
    int NUM_SHARDS = 4;
    AggregateEventShards<TransactionEvent> TAG = AggregateEventTag.sharded(TransactionEvent.class, NUM_SHARDS);

    @Override
    default AggregateEventTagger<TransactionEvent> aggregateTag() {
        return TAG;
    }

    @Value
    final class TransactionStarted implements TransactionEvent {

        private final UUID itemId;
        private final Transaction transaction;

        @JsonCreator
        public TransactionStarted(UUID itemId, Transaction transaction) {
            this.itemId = itemId;
            this.transaction = transaction;
        }
    }

    @Value
    final class DeliveryDetailsSubmitted implements TransactionEvent {
        private final UUID itemId;
        private final DeliveryData deliveryData;

        @JsonCreator
        public DeliveryDetailsSubmitted(UUID itemId, DeliveryData deliveryData) {
            this.itemId = itemId;
            this.deliveryData = deliveryData;
        }
    }

    @Value
    final class DeliveryPriceUpdated implements TransactionEvent {
        private final UUID itemId;
        private final int deliveryPrice;

        @JsonCreator
        public DeliveryPriceUpdated(UUID itemId, int deliveryPrice) {
            this.itemId = itemId;
            this.deliveryPrice = deliveryPrice;
        }
    }
}
