package com.example.auction.bidding.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import java.util.Optional;

/**
 * The auction state.
 */
public final class AuctionState implements Jsonable {

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

    public Optional<Auction> getAuction() {
        return auction;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public PSequence<Bid> getBiddingHistory() {
        return biddingHistory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuctionState that = (AuctionState) o;

        if (!auction.equals(that.auction)) return false;
        if (status != that.status) return false;
        return biddingHistory.equals(that.biddingHistory);

    }

    @Override
    public int hashCode() {
        int result = auction.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + biddingHistory.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "AuctionState{" +
                "auction=" + auction +
                ", status=" + status +
                ", biddingHistory=" + biddingHistory +
                '}';
    }
}
