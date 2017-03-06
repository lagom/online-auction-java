package com.example.auction.bidding.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.Optional;

/**
 * The auction state.
 */
@Value
public final class AuctionState implements Jsonable {

    private static final long serialVersionUID = 1L;

    /**
     * The auction details.
     */
    private final Optional<Auction> auction;
    /**
     * The status of the auction.
     */
    private final AuctionStatus status;
    /**
     * The bidding history for the auction.
     */
    private final PSequence<Bid> biddingHistory;

    @JsonCreator
    public AuctionState(Optional<Auction> auction, AuctionStatus status, PSequence<Bid> biddingHistory) {
        this.auction = auction;
        this.status = status;
        this.biddingHistory = biddingHistory;
    }

    public static AuctionState notStarted() {
        return new AuctionState(Optional.empty(), AuctionStatus.NOT_STARTED, TreePVector.empty());
    }

    public static AuctionState start(Auction auction) {
        return new AuctionState(Optional.of(auction), AuctionStatus.UNDER_AUCTION, TreePVector.empty());
    }

    public AuctionState withStatus(AuctionStatus status) {
        return new AuctionState(auction, status, biddingHistory);
    }

    public AuctionState bid(Bid bid) {
        if (lastBid().filter(b -> b.getBidder().equals(bid.getBidder())).isPresent()) {
            // Current bidder has updated their bid
            return new AuctionState(auction, status,
                    biddingHistory.minus(biddingHistory.size() - 1).plus(bid));
        } else {
            return new AuctionState(auction, status, biddingHistory.plus(bid));
        }
    }

    public Optional<Bid> lastBid() {
        if (biddingHistory.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(biddingHistory.get(biddingHistory.size() - 1));
        }
    }
}
