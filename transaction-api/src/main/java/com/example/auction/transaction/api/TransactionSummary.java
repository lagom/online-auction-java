package com.example.auction.transaction.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.UUID;

@Value
public class TransactionSummary {

    UUID itemId;
    UUID creatorId;
    UUID winnerId;
    String itemTitle;
    String currencyId;
    int itemPrice;
    TransactionInfoStatus status;

    @JsonCreator
    public TransactionSummary(UUID itemId, UUID creatorId, UUID winnerId, String itemTitle, String currencyId, int itemPrice, TransactionInfoStatus status) {
        this.itemId = itemId;
        this.creatorId = creatorId;
        this.winnerId = winnerId;
        this.itemTitle = itemTitle;
        this.currencyId = currencyId;
        this.itemPrice = itemPrice;
        this.status = status;
    }
}