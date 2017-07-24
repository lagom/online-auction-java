package com.example.auction.user.impl;

import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Value
public final class PUser implements Jsonable {

    private final UUID id;
    private final Instant createdAt;
    private final String name;
    private final String email;
    private final String passwordHash;


}
