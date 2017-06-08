package com.example.auction.transaction.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.UUID;

/**
 * A transaction command.
 */
public interface TransactionCommand extends Jsonable {

    @Value
    final class StartTransaction implements TransactionCommand, ReplyType<Done> {

        private final Transaction transaction;

        @JsonCreator
        public StartTransaction(Transaction transaction) {
            this.transaction = transaction;
        }
    }

    @Value
    final class SubmitDeliveryDetails implements TransactionCommand, ReplyType<Done> {
        private final UUID userId;
        private final DeliveryData deliveryData;

        @JsonCreator
        public SubmitDeliveryDetails(UUID userId, DeliveryData deliveryData) {
            this.userId = userId;
            this.deliveryData = deliveryData;
        }
    }

    @Value
    final class GetTransaction implements TransactionCommand, ReplyType<TransactionState> {
        private final UUID userId;

        @JsonCreator
        public GetTransaction(UUID userId) {
            this.userId = userId;
        }
    }
}
