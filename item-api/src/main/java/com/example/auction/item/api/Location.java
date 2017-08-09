package com.example.auction.item.api;

import lombok.Value;

import java.util.Optional;

@Value
public final class Location {

    private final Optional<String> country;
    private final Optional<String> state;
    private final Optional<String> city;

    public Location(Optional<String> country, Optional<String> state, Optional<String> city) {
        this.country = country;
        this.state = state;
        this.city = city;
    }
}
