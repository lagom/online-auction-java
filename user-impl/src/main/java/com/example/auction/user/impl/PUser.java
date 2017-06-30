package com.example.auction.user.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

@Value
public final class PUser implements Jsonable {

    private final UUID id;
    private final String name;
    private  final String email;

    @JsonCreator
    public PUser(@JsonProperty("id") UUID id, @JsonProperty("name") String name, @JsonProperty("email") String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

}
