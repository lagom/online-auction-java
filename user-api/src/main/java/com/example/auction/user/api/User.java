package com.example.auction.user.api;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
public final class User {

    private final UUID id;
    private final String name;
    private final String email;



}
