package com.example.auction.user.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

public interface CredentialCommand extends Jsonable {
    @Value
    final class UpdateCredential implements CredentialCommand, PersistentEntity.ReplyType<PCredential> {
        private final UUID id;
        private final String username;
        private final String password;

        @JsonCreator
        public UpdateCredential(UUID id, String username, String password) {
            this.id = id;
            this.username = username;
            this.password = password;
        }
    }

    enum GetCredential implements CredentialCommand, PersistentEntity.ReplyType<Optional<PCredential>> {
        INSTANCE
    }
}

