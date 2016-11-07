package com.example.auction.item.api;

import java.util.UUID;

/**
 * The most important fields of an item.
 */
public final class ItemSummary {

    private final UUID id;
    private final String title;
    private final String currencyId;
    private final int reservePrice;
    private final ItemStatus status;

    public ItemSummary(UUID id, String title, String currencyId, int reservePrice, ItemStatus status) {
        this.id = id;
        this.title = title;
        this.currencyId = currencyId;
        this.reservePrice = reservePrice;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public int getReservePrice() {
        return reservePrice;
    }

    public ItemStatus getStatus() {
        return status;
    }

}
