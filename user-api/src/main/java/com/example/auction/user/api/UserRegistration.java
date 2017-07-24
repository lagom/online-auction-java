package com.example.auction.user.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public final class UserRegistration {

    private final String name;
    private final String email;
    private final String password;



}
