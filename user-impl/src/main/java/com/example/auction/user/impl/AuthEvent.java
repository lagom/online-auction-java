package com.example.auction.user.impl;
import com.example.auction.user.api.Auth;
import com.example.auction.user.api.User;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

public interface AuthEvent extends Jsonable, AggregateEvent<AuthEvent> {

    @Value
    final class AuthUpdated implements AuthEvent {
        private final Auth auth;

        public AuthUpdated(Auth auth) {
            this.auth = auth;
        }
    }

    @Override
    default AggregateEventTag<AuthEvent> aggregateTag() {
        return AggregateEventTag.of(AuthEvent.class);
    }
}
