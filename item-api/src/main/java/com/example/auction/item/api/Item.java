package com.example.auction.item.api;

import org.pcollections.PSet;

import java.time.Instant;
import java.util.UUID;

/**
 * An item entity.
 */
public final class Item {

    private final UUID id;
    private final UUID creator;
    private final String title;
    private final String description;
    private final UUID categoryId;
    private final String currencyId;
    private final Location location;
    private final int reservePrice;
    private final PSet<DeliveryOption> deliveryOptions;
    private final PSet<PaymentOption> paymentOptions;
    private final int price;
    private final ItemStatus status;
    private final Instant auctionStart;
    private final Instant auctionEnd;
    private final UUID auctionWinner;

    public Item(UUID id, UUID creator, String title, String description, UUID categoryId, String currencyId, Location location, int reservePrice, PSet<DeliveryOption> deliveryOptions, PSet<PaymentOption> paymentOptions, int price, ItemStatus status, Instant auctionStart, Instant auctionEnd, UUID auctionWinner) {
        this.id = id;
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.currencyId = currencyId;
        this.location = location;
        this.reservePrice = reservePrice;
        this.deliveryOptions = deliveryOptions;
        this.paymentOptions = paymentOptions;
        this.price = price;
        this.status = status;
        this.auctionStart = auctionStart;
        this.auctionEnd = auctionEnd;
        this.auctionWinner = auctionWinner;
    }
}
