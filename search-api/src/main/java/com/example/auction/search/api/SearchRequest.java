package com.example.auction.search.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

@Value
public final class SearchRequest {

    private final Optional<String> keywords;
    private final Optional<String> category;
    private final Optional<String> country;
    private final Optional<String> state;
    private final Optional<String> city;
    private final Optional<UUID> creator;
    private final Optional<Integer> pageNo;
    private final Optional<Integer> pageSize;

    public SearchRequest(Optional<String> keywords) {
        this(keywords, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    @JsonCreator
    public SearchRequest(Optional<String> keywords, Optional<String> category, Optional<String> country, Optional<String> state, Optional<String> city, Optional<UUID> creator, Optional<Integer> pageNo, Optional<Integer> pageSize) {
        this.keywords = keywords;
        this.category = category;
        this.country = country;
        this.state = state;
        this.city = city;
        this.creator = creator;
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }
}
