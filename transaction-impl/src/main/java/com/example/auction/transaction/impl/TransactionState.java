package com.example.auction.transaction.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.Optional;
import java.util.function.Function;

/**
 * The transaction state.
 */
@Value
public class TransactionState implements Jsonable {

    private final Optional<Transaction> transaction;
    private final TransactionStatus status;

    @JsonCreator
    public TransactionState(Optional<Transaction> transaction, TransactionStatus status) {
        this.transaction = transaction;
        this.status = status;
    }

    public static TransactionState notStarted() {
        return new TransactionState(Optional.empty(), TransactionStatus.NOT_STARTED);
    }

    public static TransactionState start(Transaction transaction) {
        return new TransactionState(Optional.of(transaction), TransactionStatus.NEGOTIATING_DELIVERY);
    }

    public TransactionState updateDeliveryData(DeliveryData deliveryData) {
        return update(i -> i.withDeliveryData(deliveryData), status);
    }

    public TransactionState updateDeliveryPrice(int deliveryPrice) {
        return update(i -> i.withDeliveryPrice(deliveryPrice), status);
    }

    public TransactionState withStatus(TransactionStatus status) {
        return new TransactionState(transaction, status);
    }

    private TransactionState update(Function<Transaction, Transaction> updateFunction, TransactionStatus status) {
        assert transaction.isPresent();
        return new TransactionState(transaction.map(updateFunction), status);
    }
}
