package com.example.auction.user.impl;

import com.example.auction.user.api.User;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;

public interface UserEvent extends Jsonable, AggregateEvent<UserEvent> {

    final class UserCreated implements UserEvent {
        private final User user;

        public UserCreated(User user) {
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UserCreated that = (UserCreated) o;

            return user.equals(that.user);

        }

        @Override
        public int hashCode() {
            return user.hashCode();
        }

        @Override
        public String toString() {
            return "UserCreated{" +
                    "user=" + user +
                    '}';
        }
    }

    @Override
    default AggregateEventTag<UserEvent> aggregateTag() {
        return AggregateEventTag.of(UserEvent.class);
    }
}
