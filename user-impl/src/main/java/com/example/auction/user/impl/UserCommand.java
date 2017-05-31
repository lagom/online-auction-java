package com.example.auction.user.impl;

import com.example.auction.user.api.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.Optional;

public interface UserCommand extends Jsonable {
    @Value
    final class CreateUser implements UserCommand, PersistentEntity.ReplyType<User> {
        private final String name;
        private final String username;
        private final String password;

        @JsonCreator
        public CreateUser(String name, String username, String password) {
            this.name = name;
            this.username = username;
            this.password = password;
        }
    }

    enum GetUser implements UserCommand, PersistentEntity.ReplyType<Optional<User>> {
        INSTANCE
    }
}
