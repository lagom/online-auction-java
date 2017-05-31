package com.example.auction.transaction.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.UUID;

@Value
public class Transaction implements Jsonable {

    private final UUID itemId;
    private final UUID creator;
    private final UUID winner;
    private final int itemPrice;
    private final int deliveryPrice;

    @JsonCreator
    public Transaction(UUID itemId, UUID creator, UUID winner, int itemPrice, int deliveryPrice) {
        this.itemId = itemId;
        this.creator = creator;
        this.winner = winner;
        this.itemPrice = itemPrice;
        this.deliveryPrice = deliveryPrice;
    }
}
