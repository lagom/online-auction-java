package com.example.auction.item.api;

import lombok.Value;
import lombok.experimental.Wither;

import java.util.UUID;

/**
 * The most important fields of an item.
 */
@Value
@Wither
public final class ItemSummary {

    UUID id;
    String title;
    String currencyId;
    int reservePrice;
    ItemStatus status;

    public ItemSummary(UUID id, String title, String currencyId, int reservePrice, ItemStatus status) {
        this.id = id;
        this.title = title;
        this.currencyId = currencyId;
        this.reservePrice = reservePrice;
        this.status = status;
    }

}