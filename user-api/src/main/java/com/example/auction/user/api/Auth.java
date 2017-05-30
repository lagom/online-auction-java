package com.example.auction.user.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

@Value
public final class Auth {

    private final String username;
    private final String password;

    @JsonCreator
    public Auth(@JsonProperty("username") String username, @JsonProperty("password") String password ) {
        this.username = username;
        this.password = password;
    }

}
