package com.example.auction.user.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

public interface PUserEvent extends Jsonable, AggregateEvent<PUserEvent> {

    @Value
    final class PUserCreated implements PUserEvent {
        private final PUser user;

        public PUserCreated(PUser user) {
            this.user = user;
        }
    }

    @Override
    default AggregateEventTag<PUserEvent> aggregateTag() {
        return AggregateEventTag.of(PUserEvent.class);
    }
}
