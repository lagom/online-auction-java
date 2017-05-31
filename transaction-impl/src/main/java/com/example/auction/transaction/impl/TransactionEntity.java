package com.example.auction.transaction.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.example.auction.transaction.impl.TransactionCommand.*;
import com.example.auction.transaction.impl.TransactionEvent.*;
import java.util.Optional;
import java.util.UUID;

public class TransactionEntity extends PersistentEntity<TransactionCommand, TransactionEvent, TransactionState> {
    @Override
    public Behavior initialBehavior(Optional<TransactionState> snapshotState) {
        if (!snapshotState.isPresent()) {
            return notStarted(TransactionState.notStarted());
        } else {
            TransactionState state = snapshotState.get();
            switch (state.getStatus()) {
                case NOT_STARTED:
                    return notStarted(state);
                case NEGOTIATING_DELIVERY:
                    return negotiatingDelivery(state);
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private Behavior notStarted(TransactionState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        builder.setCommandHandler(StartTransaction.class, (start, ctx) ->
                ctx.thenPersist(new TransactionStarted(entityUUID(), start.getTransaction()), (e) ->
                        ctx.reply(Done.getInstance())
                )
        );

        builder.setEventHandlerChangingBehavior(TransactionStarted.class, started ->
                negotiatingDelivery(TransactionState.start(started.getTransaction()))
        );

        return builder.build();
    }

    private Behavior negotiatingDelivery(TransactionState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);
        // WIP ...
        return builder.build();
    }

    private UUID entityUUID() {
        return UUID.fromString(entityId());
    }
}
