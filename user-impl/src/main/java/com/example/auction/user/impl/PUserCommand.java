package com.example.auction.user.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.Optional;

public interface PUserCommand extends Jsonable {
    @Value
    final class CreatePUser implements PUserCommand, PersistentEntity.ReplyType<PUser> {
        private final String name;
        private final String email;

        @JsonCreator
        public CreatePUser(PUser user) {
            this.name = user.getName();
            this.email = user.getEmail();
        }
    }

    enum GetPUser implements PUserCommand, PersistentEntity.ReplyType<Optional<PUser>> {
        INSTANCE
    }
}
