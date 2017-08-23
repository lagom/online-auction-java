package com.example.auction.transaction.impl;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(Payment.Offline.class)
})
public abstract class Payment {

    @Value
    public static final class Offline extends Payment {
        private final String comment;
    }

}