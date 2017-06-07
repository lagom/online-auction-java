package com.example.auction.user.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

@Value
public final class User {

    private final UUID id;
    private final String email;
    private final String username;
    private final String password;

    @JsonCreator
    private User(@JsonProperty("id") Optional<UUID> id, @JsonProperty("email") String email,
                 @JsonProperty("username") String username, @JsonProperty("password") String password ) {
        this.id = id.orElse(null);
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public User(UUID id,  String username,String email, String password) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    /**
     * Used when creating a new user.
     */
    public User( String username,String email, String password) {
        this.id = null;
        this.email = email;
        this.username = username;
        this.password = password;
    }
}
