package com.example.auction.user.impl;

import com.example.auction.user.api.User;

/**
 *
 */
public class Mappers {

    public static User toApi(PUser user) {
        return new User(
                user.getId(),
                user.getCreatedAt(),
                user.getName(),
                user.getEmail()
        );
    }

}
