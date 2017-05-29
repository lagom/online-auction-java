package com.example.auction.transaction.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

/**
 * A transaction command.
 */
public interface TransactionCommand extends Jsonable {
    @Value
    final class StartTransaction implements TransactionCommand, PersistentEntity.ReplyType<Done> {

        private final Transaction transaction;

        @JsonCreator
        public StartTransaction(Transaction transaction) {
            this.transaction = transaction;
        }
    }
}
