package com.example.auction.search.api;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import lombok.Value;

@Value
public final class SearchItem {

    UUID id;
    UUID creator;
    String title;
    String description;
    String currencyId;
    OptionalInt price;
    Optional<Instant> auctionStart;
    Optional<Instant> auctionEnd;
//    UUID categoryId;


}
