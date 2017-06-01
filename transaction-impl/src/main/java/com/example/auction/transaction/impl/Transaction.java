package com.example.auction.transaction.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

@Value
public class Transaction implements Jsonable {

    private final UUID itemId;
    private final UUID creator;
    private final UUID winner;
    private final int itemPrice;
    private final int deliveryPrice;
    private final Optional<DeliveryData> deliveryData;

    @JsonCreator
    private Transaction(UUID itemId, UUID creator, UUID winner, int itemPrice, int deliveryPrice, Optional<DeliveryData> deliveryData) {
        this.itemId = itemId;
        this.creator = creator;
        this.winner = winner;
        this.itemPrice = itemPrice;
        this.deliveryPrice = deliveryPrice;
        this.deliveryData = deliveryData;
    }

    public Transaction(UUID itemId, UUID creator, UUID winner, int itemPrice) {
        this.itemId = itemId;
        this.creator = creator;
        this.winner = winner;
        this.itemPrice = itemPrice;
        this.deliveryPrice = 0;
        this.deliveryData = Optional.empty();
    }

    public Transaction withDeliveryData(DeliveryData deliveryData){
        return new Transaction(itemId, creator, winner, itemPrice, deliveryPrice, Optional.of(deliveryData));
    }
}
