package com.example.auction.user.api;

import lombok.Value;

@Value
public final class UserRegistration {

    private final String name;
    private final String email;
    private final String password;



}
