package com.example.auction.user.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.Optional;

public interface UserCommand extends Jsonable {
    @Value
    final class CreateUser implements UserCommand, PersistentEntity.ReplyType<PUser> {
        private final String name;
        private final String email;

        @JsonCreator
        public CreateUser(PUser user) {
            this.name = user.getName();
            this.email = user.getEmail();
        }
    }

    enum GetUser implements UserCommand, PersistentEntity.ReplyType<Optional<PUser>> {
        INSTANCE
    }
}
