package com.example.auction.user.impl;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

public interface CredentialEvent extends Jsonable, AggregateEvent<CredentialEvent> {

    @Value
    final class CredentialUpdated implements CredentialEvent {
        private final PCredential pCredential;

        public CredentialUpdated(PCredential pCredential) {
            this.pCredential = pCredential;
        }
    }

    @Override
    default AggregateEventTag<CredentialEvent> aggregateTag() {
        return AggregateEventTag.of(CredentialEvent.class);
    }
}
