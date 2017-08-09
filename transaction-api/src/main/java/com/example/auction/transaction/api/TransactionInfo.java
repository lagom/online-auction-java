package com.example.auction.transaction.api;

import com.example.auction.item.api.ItemData;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

@Value
public final class TransactionInfo {

    //private final PSequence<TransactionMessage> messages;
    private final UUID itemId;
    private final UUID creator;
    private final UUID winner;
    private final ItemData itemData;
    private final int itemPrice;
    private final Optional<DeliveryInfo> deliveryInfo;
    private final Optional<Integer> deliveryPrice;
    private final Optional<PaymentInfo> paymentInfo;
    private final TransactionInfoStatus status;

    @JsonCreator
    public TransactionInfo(UUID itemId, UUID creator, UUID winner, ItemData itemData, int itemPrice, Optional<DeliveryInfo> deliveryInfo, Optional<Integer> deliveryPrice, Optional<PaymentInfo> paymentInfo, TransactionInfoStatus status) {
        this.itemId = itemId;
        this.creator = creator;
        this.winner = winner;
        this.itemData = itemData;
        this.itemPrice = itemPrice;
        this.deliveryInfo = deliveryInfo;
        this.deliveryPrice = deliveryPrice;
        this.paymentInfo = paymentInfo;
        this.status = status;
    }
}