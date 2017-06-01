package com.example.auction.transaction.impl;

import akka.Done;
import com.example.auction.item.api.ItemEvent;
import com.lightbend.lagom.javadsl.api.transport.Forbidden;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.example.auction.transaction.impl.TransactionCommand.*;
import com.example.auction.transaction.impl.TransactionEvent.*;

import java.time.Instant;
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
                case PAYMENT_SUBMITTED:
                    return paymentSubmitted(state);
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private Behavior notStarted(TransactionState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        builder.setCommandHandler(StartTransaction.class, (cmd, ctx) ->
                ctx.thenPersist(new TransactionStarted(entityUUID(), cmd.getTransaction()), (e) ->
                        ctx.reply(Done.getInstance())
                )
        );

        builder.setEventHandlerChangingBehavior(TransactionStarted.class, event ->
                negotiatingDelivery(TransactionState.start(event.getTransaction()))
        );

        return builder.build();
    }

    private Behavior negotiatingDelivery(TransactionState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        builder.setReadOnlyCommandHandler(StartTransaction.class, (cmd, ctx) ->
                ctx.reply(Done.getInstance())
        );

        builder.setCommandHandler(SubmitDeliveryDetails.class, (cmd, ctx) -> {
            if(cmd.getUserId().equals(state().getTransaction().get().getWinner())) {
                return ctx.thenPersist(new DeliveryDetailsSubmitted(entityUUID(), cmd.getDeliveryData()), (e) ->
                        ctx.reply(Done.getInstance())
                );
            }
            else {
                ctx.commandFailed(new Forbidden("Only the buyer can submit delivery details"));
                return ctx.done();
            }
        });

        builder.setEventHandlerChangingBehavior(DeliveryDetailsSubmitted.class, evt ->
                paymentSubmitted(state().updateDeliveryData(evt.getDeliveryData()))
        );

        return builder.build();
    }

    private Behavior paymentSubmitted(TransactionState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);
        // WIP ...
        return builder.build();
    }

    private UUID entityUUID() {
        return UUID.fromString(entityId());
    }
}
