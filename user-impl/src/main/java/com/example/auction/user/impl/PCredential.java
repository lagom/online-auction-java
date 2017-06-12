package com.example.auction.user.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.UUID;

@Value
public final class PCredential {
    private final UUID id;
    private final String username;
    private final String password;
    @JsonCreator
    public PCredential(@JsonProperty("id") UUID id, @JsonProperty("username") String username, @JsonProperty("password") String password ) {
        this.username = username;
        this.password = password;
        this.id = id;
    }

}
