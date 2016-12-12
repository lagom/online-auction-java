package com.example.auction.search.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

@Value
public final class SearchRequest {

    private final Optional<String> keywords;
    private final Optional<Integer> maxPrice;
    private final Optional<String> currency;

    // TODO: let user select sorting of results.
    // TODO: search by category, location, status (multi-select).
//    private final Optional<String> category;
//    private final Optional<String> country;
//    private final Optional<String> state;
//    private final Optional<String> city;
//    private final Optional<UUID> creator;

    @JsonCreator
    public SearchRequest(Optional<String> keywords, Optional<Integer> maxPrice, Optional<String> currency) {
        this.keywords = keywords;
        this.maxPrice = maxPrice;
        this.currency = currency;
    }
}
