package com.example.auction.user.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

@Value
public final class User {

    private final UUID id;
    private final String name;
    private final String username;
    private final String password;

    @JsonCreator
    private User(@JsonProperty("id") Optional<UUID> id, @JsonProperty("name") String name,
                 @JsonProperty("username") String username, @JsonProperty("password") String password ) {
        this.id = id.orElse(null);
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public User(UUID id, String name, String username, String password) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    /**
     * Used when creating a new user.
     */
    public User(String name, String username, String password) {
        this.id = null;
        this.name = name;
        this.username = username;
        this.password = password;
    }
}
