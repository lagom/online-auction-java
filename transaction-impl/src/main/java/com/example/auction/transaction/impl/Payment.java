package com.example.auction.transaction.impl;

import lombok.Value;

public abstract class Payment {

    @Value
    public static final class Offline extends Payment {
        private final String comment;
    }

}