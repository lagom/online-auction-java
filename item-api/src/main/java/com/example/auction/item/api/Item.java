package com.example.auction.item.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.pcollections.PSequence;
import org.pcollections.PSet;

import java.net.URI;
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
    private final PSet<DeliveryOption>;
    private final PSet<PaymentOption>;
    private final int price;
    private final ItemStatus status;
    private final Instant auctionStart;
    private final Instant auctionEnd;
    private final UUID auctionWinner;



}
