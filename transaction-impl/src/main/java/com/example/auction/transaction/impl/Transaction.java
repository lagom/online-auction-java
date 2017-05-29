package com.example.auction.transaction.impl;

import com.example.auction.item.api.Item;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Value
public class Transaction implements Jsonable {

    private final Item item;
    private final int deliveryPrice;

    @JsonCreator
    private Transaction(Item item, int deliveryPrice) {
        this.item = item;
        this.deliveryPrice = deliveryPrice;
    }

    public Transaction(Item item) {
        this.item = item;
        this.deliveryPrice = 0;
    }
}
