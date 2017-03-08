package com.example.auction.user.impl;

import com.example.auction.user.api.User;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

public interface UserEvent extends Jsonable, AggregateEvent<UserEvent> {

    @Value
    final class UserCreated implements UserEvent {
        private final User user;

        public UserCreated(User user) {
            this.user = user;
        }
    }

    @Override
    default AggregateEventTag<UserEvent> aggregateTag() {
        return AggregateEventTag.of(UserEvent.class);
    }
}
