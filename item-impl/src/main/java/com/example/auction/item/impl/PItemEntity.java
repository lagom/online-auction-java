package com.example.auction.item.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import com.example.auction.item.impl.PItemCommand.*;
import com.example.auction.item.impl.PItemEvent.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

        builder.setReadOnlyCommandHandler(UpdateItem.class, (updateItem, ctx) ->
                // TODO: avoid using a transport Exception on PersistentEntity
                ctx.commandFailed(new NotFound(entityId()))
        );

        return builder.build();
    }

    private Behavior created(PItemState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        builder.setReadOnlyCommandHandler(GetItem.class, this::getItem);

        // must only emit an ItemUpdated when there's changes to be notified and the commander
        // is allowed to commit those changes.
        builder.setCommandHandler(UpdateItem.class, (cmd, ctx) -> {
            PItem pItem = state().getItem().get();
            return updateItem(cmd, ctx, pItem, () -> emitUpdatedEvent(cmd, ctx, pItem));
        });
        builder.setEventHandler(ItemUpdated.class, updateItemData());


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

        // must only emit an ItemUpdated if the only difference is in the description and the commander
        // is allowed to commit those changes.
        builder.setCommandHandler(UpdateItem.class,
                (cmd, ctx) -> {
                    PItem pItem = state().getItem().get();
                    return updateItem(cmd, ctx, pItem,
                            () -> {
                                if (pItem.getItemData().differOnDescriptionOnly(cmd.getItemData())) {
                                    return emitUpdatedEvent(cmd, ctx, pItem);
                                } else {
                                    ctx.commandFailed(new UpdateFailureException("During an Auction only the 'Description' may be edited."));
                                    return ctx.done();
                                }
                            }
                    );
                }
        );
        builder.setEventHandler(ItemUpdated.class, updateItemData());

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

        // a completed auction's item can't be edited.
        builder.setReadOnlyCommandHandler(UpdateItem.class, (updateItem, ctx) ->
                ctx.commandFailed(new UpdateFailureException("Can't update an item of a completed Auction." ))
        );
        // a completed auction can't be restarted.
        builder.setReadOnlyCommandHandler(StartAuction.class, (updateItem, ctx) ->
                ctx.invalidCommand("Can't reopen an auction.")
        );

        // Ignore these commands, they may come due to at least once messaging
        builder.setReadOnlyCommandHandler(UpdatePrice.class, this::alreadyDone);
        builder.setReadOnlyCommandHandler(FinishAuction.class, this::alreadyDone);

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
     * Invokes <code>onSuccess</code> if the commander in the command equals the creator of this PItem and the command payload
     * differs from the current item data. Tipically <code>onSuccess</code> will
     * be {{{@link PItemEntity#emitUpdatedEvent(UpdateItem, CommandContext, PItem)}}} but extra logic may be
     * required in some cases.
     */
    private Persist updateItem(UpdateItem cmd, CommandContext ctx, PItem pItem, Supplier<Persist> onSuccess) {
        if (!pItem.getCreator().equals(cmd.getCommander())) {
            ctx.invalidCommand("User " + cmd.getCommander() + " is not allowed to edit this auction");
            return ctx.done();
        } else if (!pItem.getItemData().equals(cmd.getItemData())) {
            return onSuccess.get();
        } else {
            // when update and current are equal there's no need to emit an event.
            return ctx.done();
        }
    }

    private Persist emitUpdatedEvent(UpdateItem cmd, CommandContext ctx, PItem pItem) {
        return ctx.thenPersist(
                new ItemUpdated(pItem.getId(), pItem.getCreator(), cmd.getItemData(), pItem.getStatus()),
                // when the command is accepted for processing we return a copy of the
                // state with the updates applied.
                evt -> ctx.reply(pItem.withDetails(cmd.getItemData())));
    }


    /**
     * convenience method to update the PItem in the PItemState with altering Instants, Status, etc...
     *
     * @return
     */
    private Function<ItemUpdated, PItemState> updateItemData() {
        return (evt) -> state().updateDetails(evt.getItemDetails());
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
