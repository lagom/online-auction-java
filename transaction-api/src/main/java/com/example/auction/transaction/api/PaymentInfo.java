package com.example.auction.transaction.api;

import com.fasterxml.jackson.annotation.*;
import lombok.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
    @JsonSubTypes.Type(PaymentInfo.Offline.class)
})
public interface PaymentInfo {

    @Value
    @JsonTypeName("payment-offline")
    final class Offline implements PaymentInfo {
        private final String comment;

        @JsonCreator
        public Offline(@JsonProperty("comment") String comment) {
            this.comment = comment;
        }
    }

}
