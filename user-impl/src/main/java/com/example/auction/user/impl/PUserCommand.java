package com.example.auction.user.impl;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.Optional;

public interface PUserCommand extends Jsonable {
    @Value
    final class CreatePUser implements PUserCommand, PersistentEntity.ReplyType<Optional<PUser>> {
        private final String name;
        private final String email;
        private final String passwordHash;
    }

    enum GetPUser implements PUserCommand, PersistentEntity.ReplyType<Optional<PUser>> {
        INSTANCE
    }
}
