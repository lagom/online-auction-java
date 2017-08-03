package com.example.auction.transaction.api;

import lombok.Value;

public abstract class PaymentInfo {

    @Value
    public static final class Offline extends PaymentInfo {
        private final String comment;
    }

}
