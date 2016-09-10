package com.example.auction.bidding.impl;

import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.example.auction.bidding.api.*;
import com.example.auction.bidding.api.Bid;
import com.example.auction.bidding.impl.AuctionCommand.*;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class BiddingServiceImpl implements BiddingService {

    private final PersistentEntityRegistry registry;

    @Inject
    public BiddingServiceImpl(PersistentEntityRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ServiceCall<Bid, BidResult> placeBid(UUID itemId) {
        return bid -> {
            PlaceBid placeBid = new PlaceBid(
                    bid.getPrice(), bid.getBidder()
            );
            return entityRef(itemId).ask(placeBid).thenApply(result ->
                        new BidResult(result.getCurrentPrice(),
                                result.getStatus().bidResultStatus, result.getCurrentBidder())
                    );
        };
    }

    @Override
    public ServiceCall<NotUsed, PSequence<Bid>> getBids(UUID itemId) {
        return request -> {
            return entityRef(itemId).ask(GetAuction.INSTANCE).thenApply(auction -> {
                List<Bid> bids = auction.getBiddingHistory().stream()
                        .map(this::convertBid)
                        .collect(Collectors.toList());
                return TreePVector.from(bids);
            });
        };
    }

    private Source<Pair<BidEvent, Offset>, ?> streamForTag(AggregateEventTag<AuctionEvent> tag, Offset offset) {
        return registry.eventStream(tag, offset).filter(eventOffset ->
                eventOffset.first() instanceof AuctionEvent.BidPlaced ||
                        eventOffset.first() instanceof AuctionEvent.BiddingFinished
        ).mapAsync(1, eventOffset -> {
            if (eventOffset.first() instanceof AuctionEvent.BidPlaced) {
                AuctionEvent.BidPlaced bid = (AuctionEvent.BidPlaced) eventOffset.first();
                return CompletableFuture.completedFuture(Pair.create(
                        new BidEvent.BidPlaced(bid.getItemId(), convertBid(bid.getBid())),
                        eventOffset.second()
                ));
            } else {
                UUID itemId = ((AuctionEvent.BiddingFinished) eventOffset.first()).getItemId();
                return getBiddingFinish(itemId, eventOffset.second());
            }
        });
    }

    private CompletionStage<Pair<BidEvent, Offset>> getBiddingFinish(UUID itemId, Offset offset) {
        return entityRef(itemId).ask(GetAuction.INSTANCE).thenApply(auction -> {
            Optional<Bid> winningBid = auction.lastBid()
                    .filter(bid ->
                            bid.getBidPrice() >= auction.getAuction().get().getReservePrice()
                    ).map(this::convertBid);
            return Pair.create(new BidEvent.BiddingFinished(itemId, winningBid), offset);
        });
    }

    private Bid convertBid(com.example.auction.bidding.impl.Bid bid) {
        return new Bid(bid.getBidder(), bid.getBidTime(), bid.getBidPrice());
    }

    private PersistentEntityRef<AuctionCommand> entityRef(UUID itemId) {
        return registry.refFor(AuctionEntity.class, itemId.toString());
    }
}
