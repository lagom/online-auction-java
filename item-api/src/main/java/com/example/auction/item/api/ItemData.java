package com.example.auction.item.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

import java.time.Duration;
import java.util.UUID;


/**
 * Data is different from Info in that Info can be inferred from data while data is source of truth. In this
 * case, ItemData is user generated and can't be restored or inferred from other sources.
 */
@Value
@Wither
public class ItemData {

    String title;
    String description;
    String currencyId;
    int increment;
    int reservePrice;
    Duration auctionDuration;

    @JsonCreator
    public ItemData(String title, String description, String currencyId, int increment, int reservePrice, Duration auctionDuration) {
        this.title = title;
        this.description = description;
        this.currencyId = currencyId;
        this.increment = increment;
        this.reservePrice = reservePrice;
        this.auctionDuration = auctionDuration;
    }
}
