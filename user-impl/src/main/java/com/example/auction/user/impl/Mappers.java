package com.example.auction.user.impl;

import com.example.auction.user.api.User;

import java.util.Optional;

public class Mappers {

    public static User toApi(Optional<PUser> user) {
        return new User(
                user.get().getId(),
                user.get().getCreatedAt(),
                user.get().getName(),
                user.get().getEmail()
        );
    }

}
