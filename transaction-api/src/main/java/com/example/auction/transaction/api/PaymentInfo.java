package com.example.auction.transaction.api;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(PaymentInfo.Offline.class)
})
public abstract class PaymentInfo {

    @Value
    public static final class Offline extends PaymentInfo {
        private final String comment;
    }

}
