package com.example.auction.user.api;

import lombok.Value;

public interface UserEvent {

    @Value
    final class PUserCreated implements UserEvent {
        private final User user;
    }


}
