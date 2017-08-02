package com.example.auction.user.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

public interface PUserEvent extends Jsonable, AggregateEvent<PUserEvent> {

    int NUM_SHARDS = 4;
    AggregateEventShards<PUserEvent> TAG = AggregateEventTag.sharded(PUserEvent.class, NUM_SHARDS);

    @Value
    final class PUserCreated implements PUserEvent {
        private final PUser user;
    }

    @Override
    default AggregateEventTagger<PUserEvent> aggregateTag() {
        return TAG;
    }
}
