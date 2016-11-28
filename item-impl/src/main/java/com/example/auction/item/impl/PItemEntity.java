package com.example.auction.item.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import com.example.auction.item.impl.PItemCommand.*;
import com.example.auction.item.impl.PItemEvent.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class PItemEntity extends PersistentEntity<PItemCommand, PItemEvent, PItemState> {
    @Override
    public Behavior initialBehavior(Optional<PItemState> snapshotState) {
        PItemStatus status = snapshotState.map(PItemState::getStatus).orElse(PItemStatus.NOT_CREATED);
        switch (status) {
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

        builder.setCommandHandler(UpdateItem.class, (create, ctx) ->
                ctx.thenPersist(new ItemUpdated(create.getItem()), evt -> ctx.reply(Done.getInstance()))
        );

        builder.setEventHandler(ItemUpdated.class, (evt) -> PItemState.create(evt.getItem()));


        builder.setCommandHandler(StartAuction.class, (start, ctx) -> {
            if (start.getUserId().equals(state().getItem().get().getCreator())) {
                return ctx.thenPersist(new AuctionStarted(entityUuid(), Instant.now()), alreadyDone(ctx));
            } else {
                ctx.invalidCommand("User " + start.getUserId() + " is not allowed to start this auction");
                return ctx.done();
            }
        });
        builder.setEventHandlerChangingBehavior(AuctionStarted.class,
                evt -> auction(state().start(evt.getStartTime())));

        return builder.build();
    }

    private Behavior auction(PItemState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        builder.setReadOnlyCommandHandler(GetItem.class, this::getItem);

        builder.setCommandHandler(UpdatePrice.class, (cmd, ctx) ->
                ctx.thenPersist(new PriceUpdated(entityUuid(), cmd.getPrice()), alreadyDone(ctx)));
        builder.setEventHandler(PriceUpdated.class, evt -> state().updatePrice(evt.getPrice()));

        builder.setCommandHandler(FinishAuction.class, (cmd, ctx) ->
                ctx.thenPersist(new AuctionFinished(entityUuid(), cmd.getWinner(), cmd.getPrice()), alreadyDone(ctx)));
        builder.setEventHandlerChangingBehavior(AuctionFinished.class,
                evt -> completed(state().end(evt.getWinner(), evt.getPrice())));

        // Ignored commands
        builder.setReadOnlyCommandHandler(StartAuction.class, this::alreadyDone);


        return builder.build();
    }

    private Behavior completed(PItemState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        builder.setReadOnlyCommandHandler(GetItem.class, this::getItem);


        // Ignore these commands, they may come due to at least once messaging
        builder.setReadOnlyCommandHandler(UpdatePrice.class, this::alreadyDone);
        builder.setReadOnlyCommandHandler(FinishAuction.class, this::alreadyDone);

        // a completed auction can't be restarted. Don't fail this command because it may
        // be a dupe due to at least once messaging.
        builder.setReadOnlyCommandHandler(StartAuction.class, this::alreadyDone);

        return builder.build();
    }

    private Behavior cancelled(PItemState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        builder.setReadOnlyCommandHandler(GetItem.class, this::getItem);

        // Ignore these commands, they may come due to at least once messaging
        builder.setReadOnlyCommandHandler(UpdatePrice.class, this::alreadyDone);
        builder.setReadOnlyCommandHandler(FinishAuction.class, this::alreadyDone);

        return builder.build();
    }

    /**
     * Convenience method to handle commands which have already been processed (idempotent processing).
     * TODO: review naming. See AuctionEvent#alreadyDone in bidding-impl project.
     */
    private void alreadyDone(Object command, ReadOnlyCommandContext<Done> ctx) {
        ctx.reply(Done.getInstance());
    }

    /**
     * Convenience method to handle commands which have already been processed (idempotent processing).
     * TODO: review naming. See AuctionEvent#alreadyDone in bidding-impl project.
     */
    private <T> Consumer<T> alreadyDone(ReadOnlyCommandContext<Done> ctx) {
        return (evt) -> ctx.reply(Done.getInstance());
    }

    private void getItem(GetItem get, ReadOnlyCommandContext<Optional<PItem>> ctx) {
        ctx.reply(state().getItem());
    }

    private UUID entityUuid() {
        return UUID.fromString(entityId());
    }
}
