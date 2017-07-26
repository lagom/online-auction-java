package com.example.auction.transaction.impl;

import com.example.auction.item.api.ItemData;
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
    private final ItemData itemData;
    private final int itemPrice;
    private final Optional<Integer> deliveryPrice;
    private final Optional<DeliveryData> deliveryData;

    @JsonCreator
    private Transaction(UUID itemId, UUID creator, UUID winner, ItemData itemData, int itemPrice, Optional<Integer> deliveryPrice, Optional<DeliveryData> deliveryData) {
        this.itemId = itemId;
        this.creator = creator;
        this.winner = winner;
        this.itemData = itemData;
        this.itemPrice = itemPrice;
        this.deliveryPrice = deliveryPrice;
        this.deliveryData = deliveryData;
    }

    public Transaction(UUID itemId, UUID creator, UUID winner, ItemData itemData, int itemPrice) {
        this.itemId = itemId;
        this.creator = creator;
        this.winner = winner;
        this.itemData = itemData;
        this.itemPrice = itemPrice;
        this.deliveryPrice = Optional.empty();
        this.deliveryData = Optional.empty();
    }

    public Transaction withDeliveryData(DeliveryData deliveryData) {
        return new Transaction(itemId, creator, winner, itemData, itemPrice, deliveryPrice, Optional.of(deliveryData));
    }

    public Transaction withDeliveryPrice(int deliveryPrice) {
        return new Transaction(itemId, creator, winner, itemData, itemPrice, Optional.of(deliveryPrice), deliveryData);
    }
}
