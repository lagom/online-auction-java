package com.example.auction.search.api;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

@Value
public final class SearchItem {

    UUID id;
    UUID creator;
    String title;
    String description;
    String itemStatus;
    String currencyId;
    Optional<Integer> price;
    Optional<Instant> auctionStart;
    Optional<Instant> auctionEnd;
//    UUID categoryId;


    @JsonCreator
    public SearchItem(UUID id, UUID creator, String title, String description, String itemStatus, String currencyId, Optional<Integer> price, Optional<Instant> auctionStart, Optional<Instant> auctionEnd) {
        this.id = id;
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.itemStatus = itemStatus;
        this.currencyId = currencyId;
        this.price = price;
        this.auctionStart = auctionStart;
        this.auctionEnd = auctionEnd;
    }
}
