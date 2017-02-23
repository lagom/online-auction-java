package com.example.auction.bidding.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import com.example.auction.bidding.impl.AuctionCommand.*;
import com.example.auction.bidding.impl.AuctionEvent.*;

/**
 * The auction persistent entity.
 */
public class AuctionEntity extends PersistentEntity<AuctionCommand, AuctionEvent, AuctionState> {

    @Override
    public Behavior initialBehavior(Optional<AuctionState> snapshotState) {
        if (!snapshotState.isPresent()) {
            return notStarted(AuctionState.notStarted());
        } else {
            AuctionState state = snapshotState.get();
            switch (state.getStatus()) {
                case NOT_STARTED:
                    return notStarted(state);
                case UNDER_AUCTION:
                    return underAuction(state);
                case COMPLETE:
                    return completed(state);
                case CANCELLED:
                    return cancelled(state);
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private void addCancelHandlers(BehaviorBuilder builder) {
        builder.setEventHandlerChangingBehavior(AuctionCancelled.class, cancel ->
                cancelled(state().withStatus(AuctionStatus.CANCELLED))
        );
        builder.setCommandHandler(CancelAuction.class, (cancel, ctx) ->
                persistAndDone(ctx, new AuctionCancelled(entityUUID()))
        );
    }

    private void addGetAuctionHandler(BehaviorBuilder builder) {
        builder.setReadOnlyCommandHandler(GetAuction.class, (cmd, ctx) ->
                ctx.reply(state())
        );
    }

    /**
     * Behavior for the not started state.
     */
    private Behavior notStarted(AuctionState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        // Command handlers
        builder.setCommandHandler(StartAuction.class, (start, ctx) ->
                persistAndDone(ctx, new AuctionStarted(entityUUID(), start.getAuction()))
        );
        builder.setReadOnlyCommandHandler(PlaceBid.class, (bid, ctx) ->
                ctx.reply(createResult(PlaceBidStatus.NOT_STARTED))
        );
        addGetAuctionHandler(builder);

        // Event handlers
        builder.setEventHandlerChangingBehavior(AuctionStarted.class, started ->
                underAuction(AuctionState.start(started.getAuction()))
        );

        addCancelHandlers(builder);

        return builder.build();
    }

    /**
     * Behavior for the under auction state.
     */
    private Behavior underAuction(AuctionState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        // Command handlers
        builder.setReadOnlyCommandHandler(StartAuction.class, this::alreadyDone);
        builder.setCommandHandler(PlaceBid.class, this::handlePlaceBidWhileUnderAuction);
        builder.setCommandHandler(FinishBidding.class, (finish, ctx) ->
                persistAndDone(ctx, new BiddingFinished(entityUUID()))
        );
        addGetAuctionHandler(builder);

        // Event handlers
        builder.setEventHandler(BidPlaced.class, bidPlaced ->
                state().bid(bidPlaced.getBid())
        );
        builder.setEventHandlerChangingBehavior(BiddingFinished.class, finished ->
                completed(state().withStatus(AuctionStatus.COMPLETE))
        );

        addCancelHandlers(builder);

        return builder.build();
    }

    /**
     * Behavior for the completed state.
     */
    private Behavior completed(AuctionState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        // Command handlers
        builder.setReadOnlyCommandHandler(StartAuction.class, this::alreadyDone);
        builder.setReadOnlyCommandHandler(FinishBidding.class, this::alreadyDone);
        builder.setReadOnlyCommandHandler(PlaceBid.class, (bid, ctx) ->
                ctx.reply(createResult(PlaceBidStatus.FINISHED))
        );
        addGetAuctionHandler(builder);

        // Event handlers
        // Note, an item can go from completed to cancelled, since it is the item service that controls
        // whether an auction is cancelled or not. If it cancels before it receives a bidding finished
        // event from us, it will ignore the bidding finished event, so we need to update our state
        // to reflect that.
        addCancelHandlers(builder);

        return builder.build();
    }

    /**
     * Behavior for the cancelled state.
     */
    private Behavior cancelled(AuctionState state) {
        BehaviorBuilder builder = newBehaviorBuilder(state);

        // Command handlers
        builder.setReadOnlyCommandHandler(StartAuction.class, this::alreadyDone);
        builder.setReadOnlyCommandHandler(FinishBidding.class, this::alreadyDone);
        builder.setReadOnlyCommandHandler(CancelAuction.class, this::alreadyDone);
        builder.setReadOnlyCommandHandler(PlaceBid.class, (bid, ctx) ->
                ctx.reply(createResult(PlaceBidStatus.CANCELLED))
        );
        addGetAuctionHandler(builder);

        return builder.build();
    }

    /**
     * Convenience method to handle when a command has already been processed (idempotent processing).
     */
    private void alreadyDone(Object command, ReadOnlyCommandContext<Done> ctx) {
        ctx.reply(Done.getInstance());
    }

    /**
     * Persist a single event then respond with done.
     */
    private Persist<AuctionEvent> persistAndDone(CommandContext<Done> ctx, AuctionEvent event) {
        return ctx.thenPersist(event, (e) -> ctx.reply(Done.getInstance()));
    }

    /**
     * The main logic for handling of bids.
     */
    private Persist<AuctionEvent> handlePlaceBidWhileUnderAuction(PlaceBid bid, CommandContext<PlaceBidResult> ctx) {
        Auction auction = state().getAuction().get();

        Instant now = Instant.now();

        // Even though we're not in the finished state yet, we should check
        if (auction.getEndTime().isBefore(now)) {
            return reply(ctx, createResult(PlaceBidStatus.FINISHED));
        }

        if (auction.getCreator().equals(bid.getBidder())) {
            throw new BidValidationException("An auctions creator cannot bid in their own auction.");
        }

        Optional<Bid> currentBid = state().lastBid();
        int currentBidPrice;
        int currentBidMaximum;
        if (currentBid.isPresent()) {
            currentBidPrice = currentBid.get().getBidPrice();
            currentBidMaximum = currentBid.get().getMaximumBid();
        } else {
            currentBidPrice = 0;
            currentBidMaximum = 0;
        }

        boolean bidderIsCurrentBidder = currentBid.filter(b -> b.getBidder().equals(bid.getBidder())).isPresent();

        if (bidderIsCurrentBidder && bid.getBidPrice() >= currentBidPrice) {
            // Allow the current bidder to update their bid
            if (auction.getReservePrice()>currentBidPrice) {

                int newBidPrice = Math.min(auction.getReservePrice(), bid.getBidPrice());
                PlaceBidStatus placeBidStatus;

                if (newBidPrice == auction.getReservePrice()) { 
                    placeBidStatus = PlaceBidStatus.ACCEPTED;
                }
                else { 
                    placeBidStatus = PlaceBidStatus.ACCEPTED_BELOW_RESERVE;
                }    
                return ctx.thenPersist(new BidPlaced(entityUUID(),
                        new Bid(bid.getBidder(), now, newBidPrice, bid.getBidPrice())), (e) ->
                        ctx.reply(new PlaceBidResult(placeBidStatus, newBidPrice, bid.getBidder()))
                );
            }
                return ctx.thenPersist(new BidPlaced(entityUUID(),
                        new Bid(bid.getBidder(), now, currentBidPrice, bid.getBidPrice())), (e) ->
                        ctx.reply(new PlaceBidResult(PlaceBidStatus.ACCEPTED, currentBidPrice, bid.getBidder()))
                );
        }

        if (bid.getBidPrice() < currentBidPrice + auction.getIncrement()) {
            return reply(ctx, createResult(PlaceBidStatus.TOO_LOW));
        } else if (bid.getBidPrice() <= currentBidMaximum) {
            return handleAutomaticOutbid(bid, ctx, auction, now, currentBid, currentBidPrice, currentBidMaximum);
        } else {
            return handleNewWinningBidder(bid, ctx, auction, now, currentBidMaximum);
        }
    }

    /**
     * Handle the situation where a bid will be accepted, but it will be automatically outbid by the current bidder.
     *
     * This emits two events, one for the bid currently being replace, and another automatic bid for the current bidder.
     */
    private Persist<AuctionEvent> handleAutomaticOutbid(PlaceBid bid, CommandContext<PlaceBidResult> ctx, Auction auction,
            Instant now, Optional<Bid> currentBid, int currentBidPrice, int currentBidMaximum) {
        // Adjust the bid so that the increment for the current maximum makes the current maximum a valid bid
        int adjustedBidPrice = Math.min(bid.getBidPrice(), currentBidMaximum - auction.getIncrement());
        int newBidPrice = adjustedBidPrice + auction.getIncrement();

        return ctx.thenPersistAll(Arrays.asList(
                new BidPlaced(entityUUID(),
                        new Bid(bid.getBidder(), now, adjustedBidPrice, bid.getBidPrice())
                ),
                new BidPlaced(entityUUID(),
                        new Bid(currentBid.get().getBidder(), now, newBidPrice, currentBidMaximum)
                )
        ), () -> {
            ctx.reply(new PlaceBidResult(PlaceBidStatus.ACCEPTED_OUTBID, newBidPrice, currentBid.get().getBidder()));
        });
    }

    /**
     * Handle the situation where a bid will be accepted as the new winning bidder.
     */
    private Persist<AuctionEvent> handleNewWinningBidder(PlaceBid bid, CommandContext<PlaceBidResult> ctx,
            Auction auction, Instant now, int currentBidMaximum) {
        int nextIncrement = Math.min(currentBidMaximum + auction.getIncrement(), bid.getBidPrice());
        int newBidPrice;
        if (nextIncrement < auction.getReservePrice()) {
            newBidPrice = Math.min(auction.getReservePrice(), bid.getBidPrice());
        } else {
            newBidPrice = nextIncrement;
        }
        return ctx.thenPersist(new BidPlaced(
                entityUUID(),
                new Bid(bid.getBidder(), now, newBidPrice, bid.getBidPrice())
        ), (e) -> {
            PlaceBidStatus status;
            if (newBidPrice < auction.getReservePrice()) {
                status = PlaceBidStatus.ACCEPTED_BELOW_RESERVE;
            } else {
                status = PlaceBidStatus.ACCEPTED;
            }
            ctx.reply(new PlaceBidResult(status, newBidPrice, bid.getBidder()));
        });
    }

    private UUID entityUUID() {
        return UUID.fromString(entityId());
    }

    private Persist<AuctionEvent> reply(CommandContext<PlaceBidResult> ctx, PlaceBidResult result) {
        ctx.reply(result);
        return ctx.done();
    }

    private PlaceBidResult createResult(PlaceBidStatus status) {
        if (state().getBiddingHistory().size() > 0) {
            Bid bid = state().getBiddingHistory().get(state().getBiddingHistory().size() - 1);
            return new PlaceBidResult(status, bid.getBidPrice(), bid.getBidder());
        } else {
            return new PlaceBidResult(status, 0, null);
        }
    }

}
