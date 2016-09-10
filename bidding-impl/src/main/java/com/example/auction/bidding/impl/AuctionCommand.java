package com.example.auction.bidding.impl;

import akka.Done;
import com.example.auction.bidding.api.BidResultStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity.ReplyType;
import com.lightbend.lagom.serialization.Jsonable;
import org.pcollections.PSequence;

import java.util.UUID;

/**
 * An auction command.
 */
public interface AuctionCommand extends Jsonable {

    /**
     * Start the auction.
     */
    final class StartAuction implements AuctionCommand, ReplyType<Done> {
        /**
         * The auction to start.
         */
        private final Auction auction;

        @JsonCreator
        public StartAuction(Auction auction) {
            this.auction = auction;
        }

        public Auction getAuction() {
            return auction;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StartAuction that = (StartAuction) o;

            return auction.equals(that.auction);

        }

        @Override
        public int hashCode() {
            return auction.hashCode();
        }

        @Override
        public String toString() {
            return "StartAuction{" +
                    "auction=" + auction +
                    '}';
        }
    }

    /**
     * Cancel the auction.
     */
    enum CancelAuction implements AuctionCommand, ReplyType<Done> {
        INSTANCE
    }

    /**
     * Place a bid on the auction.
     */
    final class PlaceBid implements AuctionCommand, ReplyType<PlaceBidResult> {
        private final int bidPrice;
        private final UUID bidder;

        public PlaceBid(int bidPrice, UUID bidder) {
            this.bidPrice = bidPrice;
            this.bidder = bidder;
        }

        public int getBidPrice() {
            return bidPrice;
        }

        public UUID getBidder() {
            return bidder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PlaceBid placeBid = (PlaceBid) o;

            if (bidPrice != placeBid.bidPrice) return false;
            return bidder.equals(placeBid.bidder);

        }

        @Override
        public int hashCode() {
            int result = bidPrice;
            result = 31 * result + bidder.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "PlaceBid{" +
                    "bidPrice=" + bidPrice +
                    ", bidder=" + bidder +
                    '}';
        }
    }

    /**
     * The status of placing a bid.
     */
    enum PlaceBidStatus {
        /**
         * The bid was accepted, and is the current highest bid.
         */
        ACCEPTED(BidResultStatus.ACCEPTED),
        /**
         * The bid was accepted, but was outbidded by the maximum bid of the current highest bidder.
         */
        ACCEPTED_OUTBID(BidResultStatus.ACCEPTED_OUTBID),
        /**
         * The bid was accepted, but was not at least the auction increment above the maximum bid.
         */
        ACCEPTED_BELOW_INCREMENT(BidResultStatus.ACCEPTED_BELOW_INCREMENT),
        /**
         * The bid was accepted, but is below the reserve.
         */
        ACCEPTED_BELOW_RESERVE(BidResultStatus.ACCEPTED_BELOW_RESERVE),
        /**
         * The bid was not at least the current bid plus the increment.
         */
        TOO_LOW(BidResultStatus.TOO_LOW),
        /**
         * The auction hasn't started.
         */
        NOT_STARTED(BidResultStatus.NOT_STARTED),
        /**
         * The auction has already finished.
         */
        FINISHED(BidResultStatus.FINISHED),
        /**
         * The auction has been cancelled.
         */
        CANCELLED(BidResultStatus.CANCELLED);

        public final BidResultStatus bidResultStatus;

        PlaceBidStatus(BidResultStatus bidResultStatus) {
            this.bidResultStatus = bidResultStatus;
        }

        public static PlaceBidStatus from(BidResultStatus status) {
            switch (status) {
                case ACCEPTED:
                    return ACCEPTED;
                case ACCEPTED_BELOW_INCREMENT:
                    return ACCEPTED_BELOW_INCREMENT;
                case ACCEPTED_BELOW_RESERVE:
                    return ACCEPTED_BELOW_RESERVE;
                case ACCEPTED_OUTBID:
                    return ACCEPTED_OUTBID;
                case CANCELLED:
                    return CANCELLED;
                case FINISHED:
                    return FINISHED;
                case NOT_STARTED:
                    return NOT_STARTED;
                case TOO_LOW:
                    return TOO_LOW;
                default:
                    throw new IllegalStateException();
            }
        }
    }

    /**
     * The result of placing a bid.
     */
    final class PlaceBidResult implements Jsonable {
        /**
         * The current price of the auction.
         */
        private final int currentPrice;
        /**
         * The status of the attempt to place a bid.
         */
        private final PlaceBidStatus status;
        /**
         * The current winning bidder.
         */
        private final UUID currentBidder;

        @JsonCreator
        public PlaceBidResult(PlaceBidStatus status, int currentPrice, UUID currentBidder) {
            this.currentPrice = currentPrice;
            this.status = status;
            this.currentBidder = currentBidder;
        }

        public int getCurrentPrice() {
            return currentPrice;
        }

        public PlaceBidStatus getStatus() {
            return status;
        }

        public UUID getCurrentBidder() {
            return currentBidder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PlaceBidResult that = (PlaceBidResult) o;

            if (currentPrice != that.currentPrice) return false;
            if (status != that.status) return false;
            return currentBidder != null ? currentBidder.equals(that.currentBidder) : that.currentBidder == null;

        }

        @Override
        public int hashCode() {
            int result = currentPrice;
            result = 31 * result + status.hashCode();
            result = 31 * result + (currentBidder != null ? currentBidder.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "PlaceBidResult{" +
                    "currentPrice=" + currentPrice +
                    ", status=" + status +
                    ", currentBidder=" + currentBidder +
                    '}';
        }
    }

    /**
     * Finish bidding.
     */
    enum FinishBidding implements AuctionCommand, ReplyType<Done> {
        INSTANCE
    }

    /**
     * Get the auction.
     */
    enum GetAuction implements AuctionCommand, ReplyType<AuctionState> {
        INSTANCE
    }
}
