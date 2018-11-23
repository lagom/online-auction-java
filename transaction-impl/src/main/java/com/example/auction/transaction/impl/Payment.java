package com.example.auction.transaction.impl;

import com.fasterxml.jackson.annotation.*;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.EqualsAndHashCode;
import lombok.Value;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({
    @JsonSubTypes.Type(Payment.Offline.class)
})
public interface Payment extends Jsonable {

    @Value
    @EqualsAndHashCode(callSuper = false)
    @JsonTypeName("payment-offline")
    final class Offline implements Payment {
        private final String comment;

        @JsonCreator
        public Offline(@JsonProperty("comment") String comment) {
            this.comment = comment;
        }
    }

}
