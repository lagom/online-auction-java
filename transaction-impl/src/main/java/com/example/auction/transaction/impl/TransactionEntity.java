package com.example.auction.transaction.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.api.transport.Forbidden;
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

        addGetTransactionHandler(builder);

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
            else
                throw new Forbidden("Only the auction winner can submit delivery details");
        });

        builder.setEventHandler(DeliveryDetailsSubmitted.class, evt ->
                state().updateDeliveryData(evt.getDeliveryData())
        );

        addGetTransactionHandler(builder);

        return builder.build();
    }

    private Behavior paymentSubmitted(TransactionState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);
        // WIP ...

        addGetTransactionHandler(builder);

        return builder.build();
    }

    private void addGetTransactionHandler(BehaviorBuilder builder) {
        builder.setReadOnlyCommandHandler(GetTransaction.class, (cmd, ctx) -> {
            if(cmd.getUserId().equals(state().getTransaction().get().getCreator()) ||
                    cmd.getUserId().equals(state().getTransaction().get().getWinner()))
                    ctx.reply(state());
            else
                throw new Forbidden("Only the item owner and the auction winner can see transaction details");
        });
    }

    private UUID entityUUID() {
        return UUID.fromString(entityId());
    }
}
