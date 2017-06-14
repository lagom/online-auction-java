package com.example.auction.user.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;
import java.util.UUID;

import static com.example.auction.user.impl.CredentialCommand.UpdateCredential.workload;

public interface CredentialCommand extends Jsonable {
    @Value
    final class UpdateCredential implements CredentialCommand, PersistentEntity.ReplyType<Done> {
        private final UUID id;
        private final String username;
        private final String password;
        public static int workload = 12;

        @JsonCreator
        public UpdateCredential(UUID id, String username, String password) {
            this.id = id;
            this.username = username;
            this.password = password;
        }
    }
        public static String hashPassword(String password_plaintext) {
        String salt = BCrypt.gensalt(workload);
        String hashed_password = BCrypt.hashpw(password_plaintext, salt);

        return(hashed_password);
    }
        public static boolean checkPassword(String password_plaintext, String stored_hash) {
        boolean password_verified = false;

        if(null == stored_hash)
            throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison");

        password_verified = BCrypt.checkpw(password_plaintext, stored_hash);

        return(password_verified);
    }
    enum Login implements CredentialCommand, PersistentEntity.ReplyType<Optional<PCredential>> {
        INSTANCE
    }
}

