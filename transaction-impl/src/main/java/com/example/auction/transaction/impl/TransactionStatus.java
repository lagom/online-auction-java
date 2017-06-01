package com.example.auction.transaction.impl;

public enum TransactionStatus {
    NOT_STARTED,
    NEGOTIATING_DELIVERY,
    PAYMENT_SUBMITTED,
    PAYMENT_FAILED,
    PAYMENT_CONFIRMED,
    ITEM_DISPATCHED,
    ITEM_RECEIVED,
    CANCELED,
    REFUNDING,
    REFUNDED
}
