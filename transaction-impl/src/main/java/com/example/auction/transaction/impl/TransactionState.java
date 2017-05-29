package com.example.auction.transaction.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;
import org.pcollections.PSequence;

import java.util.Optional;

/**
 * The transaction state.
 */
@Value
public class TransactionState implements Jsonable {

    /**
     * The transaction details.
     */
    private final Optional<Transaction> transaction;
    private final TransactionStatus status;

    @JsonCreator
    public TransactionState(Optional<Transaction> transaction, TransactionStatus status) {
        this.transaction = transaction;
        this.status = status;
    }
}
