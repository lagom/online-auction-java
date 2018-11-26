package com.example.auction.transaction.api;

public interface PaymentEvent {

    final class PaymentDetailsSubmitted implements PaymentEvent {
    }

    final class RefundInitiated implements PaymentEvent {
    }

}
