package com.example.auction.user.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Value
public final class User {

    private final UUID id;
    private final Timestamp createdAt;
    private final String name;
    private final String email;

    @JsonCreator
    public User(@JsonProperty("id") UUID id, @JsonProperty("createdAt") Timestamp createdAt , @JsonProperty("name") String name, @JsonProperty("email") String email) {
        this.id = id;
        this.createdAt = createdAt;
        this.name = name;
        this.email = email;
    }

}
