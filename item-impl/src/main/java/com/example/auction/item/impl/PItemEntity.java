package com.example.auction.item.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import com.example.auction.item.impl.PItemCommand.*;
import com.example.auction.item.impl.PItemEvent.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class PItemEntity extends PersistentEntity<PItemCommand, PItemEvent, PItemState> {
    @Override
    public Behavior initialBehavior(Optional<PItemState> snapshotState) {
        PItemStatus status = snapshotState.map(PItemState::getStatus).orElse(PItemStatus.NOT_CREATED);
        switch(status) {
            case NOT_CREATED:
                return empty();
            case CREATED:
                return created(snapshotState.get());
            case AUCTION:
                return auction(snapshotState.get());
            case COMPLETED:
                return completed(snapshotState.get());
            case CANCELLED:
                return cancelled(snapshotState.get());
            default:
                throw new IllegalStateException("Unknown status: " + status);
        }
    }

    private Behavior empty() {
        BehaviorBuilder builder = newBehaviorBuilder(PItemState.empty());

        builder.setReadOnlyCommandHandler(GetItem.class, this::getItem);
        // maybe do some validation? Eg, check that UUID of item matches entity UUID...
        builder.setCommandHandler(CreateItem.class, (create, ctx) ->
                ctx.thenPersist(new ItemCreated(create.getItem()), evt -> ctx.reply(Done.getInstance()))
        );

        builder.setEventHandlerChangingBehavior(ItemCreated.class, evt -> created(PItemState.create(evt.getItem())));

        return builder.build();
    }

    private Behavior created(PItemState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        builder.setReadOnlyCommandHandler(GetItem.class, this::getItem);

        builder.setCommandHandler(StartAuction.class, (start, ctx) ->
                ctx.thenPersist(new AuctionStarted(entityUuid()), evt -> ctx.reply(Done.getInstance()))
        );
        builder.setEventHandlerChangingBehavior(AuctionStarted.class, evt -> auction(state().start(Instant.now())));

        return builder.build();
    }

    private Behavior auction(PItemState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        builder.setReadOnlyCommandHandler(GetItem.class, this::getItem);

        return builder.build();
    }

    private Behavior completed(PItemState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        builder.setReadOnlyCommandHandler(GetItem.class, this::getItem);

        return builder.build();
    }

    private Behavior cancelled(PItemState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        builder.setReadOnlyCommandHandler(GetItem.class, this::getItem);

        return builder.build();
    }

    private void getItem(GetItem get, ReadOnlyCommandContext<Optional<PItem>> ctx) {
        ctx.reply(state().getItem());
    }

    private UUID entityUuid() {
        return UUID.fromString(entityId());
    }
}
