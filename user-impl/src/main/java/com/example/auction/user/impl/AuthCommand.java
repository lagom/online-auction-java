package com.example.auction.user.impl;

import com.example.auction.user.api.Auth;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

public interface AuthCommand extends Jsonable {
    @Value
    final class UpdateAuth implements AuthCommand, PersistentEntity.ReplyType<Auth> {
        private final UUID id;
        private final String username;
        private final String password;

        @JsonCreator
        public UpdateAuth(UUID id, String username, String password) {
            this.id = id;
            this.username = username;
            this.password = password;
        }
    }

    enum GetAuth implements AuthCommand, PersistentEntity.ReplyType<Optional<Auth>> {
        INSTANCE
    }
}

