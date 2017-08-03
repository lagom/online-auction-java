package com.example.auction.transaction.impl;

import com.example.auction.transaction.api.TransactionInfoStatus;

public enum TransactionStatus {
    NOT_STARTED(null),
    NEGOTIATING_DELIVERY(TransactionInfoStatus.NEGOTIATING_DELIVERY),
    PAYMENT_PENDING(TransactionInfoStatus.PAYMENT_PENDING),
    PAYMENT_SUBMITTED(TransactionInfoStatus.PAYMENT_SUBMITTED),
    PAYMENT_FAILED(TransactionInfoStatus.PAYMENT_FAILED),
    PAYMENT_CONFIRMED(TransactionInfoStatus.PAYMENT_CONFIRMED),
    ITEM_DISPATCHED(TransactionInfoStatus.ITEM_DISPATCHED),
    ITEM_RECEIVED(TransactionInfoStatus.ITEM_RECEIVED),
    CANCELLED(TransactionInfoStatus.CANCELLED),
    REFUNDING(TransactionInfoStatus.REFUNDING),
    REFUNDED(TransactionInfoStatus.REFUNDED);

    public final TransactionInfoStatus transactionInfoStatus;

    TransactionStatus(TransactionInfoStatus transactionInfoStatus) {
        this.transactionInfoStatus = transactionInfoStatus;
    }
}
