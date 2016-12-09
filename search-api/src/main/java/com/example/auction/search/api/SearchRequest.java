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
    private final Optional<String> category;
    private final Optional<String> country;
    private final Optional<String> state;
    private final Optional<String> city;
    private final Optional<UUID> creator;

    public SearchRequest(Optional<String> keywords, Optional<Integer> maxPrice, Optional<String> currency) {
        this(keywords, maxPrice, currency, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    @JsonCreator
    public SearchRequest(Optional<String> keywords, Optional<Integer> maxPrice, Optional<String> currency, Optional<String> category, Optional<String> country, Optional<String> state, Optional<String> city, Optional<UUID> creator) {
        this.keywords = keywords;
        this.maxPrice = maxPrice;
        this.currency = currency;
        this.category = category;
        this.country = country;
        this.state = state;
        this.city = city;
        this.creator = creator;
    }
}
