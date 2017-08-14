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
    private final Optional<DeliveryData> deliveryData;
    private final Optional<Integer> deliveryPrice;
    private final Optional<Payment> payment;

    @JsonCreator
    private Transaction(UUID itemId, UUID creator, UUID winner, ItemData itemData, int itemPrice, Optional<DeliveryData> deliveryData, Optional<Integer> deliveryPrice, Optional<Payment> payment) {
        this.itemId = itemId;
        this.creator = creator;
        this.winner = winner;
        this.itemData = itemData;
        this.itemPrice = itemPrice;
        this.deliveryData = deliveryData;
        this.deliveryPrice = deliveryPrice;
        this.payment = payment;
    }

    public Transaction(UUID itemId, UUID creator, UUID winner, ItemData itemData, int itemPrice) {
        this.itemId = itemId;
        this.creator = creator;
        this.winner = winner;
        this.itemData = itemData;
        this.itemPrice = itemPrice;
        this.deliveryData = Optional.empty();
        this.deliveryPrice = Optional.empty();
        this.payment = Optional.empty();
    }

    public Transaction withDeliveryData(DeliveryData deliveryData) {
        return new Transaction(itemId, creator, winner, itemData, itemPrice, Optional.of(deliveryData), deliveryPrice, payment);
    }

    public Transaction withDeliveryPrice(int deliveryPrice) {
        return new Transaction(itemId, creator, winner, itemData, itemPrice, deliveryData, Optional.of(deliveryPrice), payment);
    }

    public Transaction withPayment(Payment payment) {
        return new Transaction(itemId, creator, winner, itemData, itemPrice, deliveryData, deliveryPrice, Optional.of(payment));
    }
}