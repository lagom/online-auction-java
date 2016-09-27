package com.example.auction.bidding.impl;

import akka.Done;
import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import com.example.auction.bidding.api.*;
import com.example.auction.bidding.api.Bid;
import com.example.auction.bidding.api.PlaceBid;
import com.example.auction.bidding.impl.AuctionCommand.GetAuction;
import com.example.auction.item.api.ItemEvent;
import com.example.auction.item.api.ItemService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static com.example.auction.security.ServerSecurity.authenticated;

@Singleton
public class BiddingServiceImpl implements BiddingService {

    private final PersistentEntityRegistry registry;

    @Inject
    public BiddingServiceImpl(PersistentEntityRegistry registry, ItemService itemService) {
        this.registry = registry;

        registry.register(AuctionEntity.class);

        itemService.itemEvents().subscribe().atLeastOnce(Flow.<ItemEvent>create().mapAsync(1, itemEvent -> {
            if (itemEvent instanceof ItemEvent.AuctionStarted) {
                ItemEvent.AuctionStarted auctionStarted = (ItemEvent.AuctionStarted) itemEvent;
                Auction auction = new Auction(auctionStarted.getItemId(), auctionStarted.getCreator(),
                        auctionStarted.getReservePrice(), auctionStarted.getIncrement(), auctionStarted.getStartDate(),
                        auctionStarted.getEndDate());

                return entityRef(auctionStarted.getItemId()).ask(new AuctionCommand.StartAuction(auction));
            } else if (itemEvent instanceof ItemEvent.AuctionCancelled) {
                return entityRef(itemEvent.getItemId()).ask(AuctionCommand.CancelAuction.INSTANCE);
            } else {
                return CompletableFuture.completedFuture(Done.getInstance());
            }
        }));
    }

    @Override
    public ServiceCall<PlaceBid, BidResult> placeBid(UUID itemId) {
        return authenticated(userId -> bid -> {
            AuctionCommand.PlaceBid placeBid = new AuctionCommand.PlaceBid(bid.getMaximumBidPrice(), userId);
            return entityRef(itemId).ask(placeBid).thenApply(result ->
                        new BidResult(result.getCurrentPrice(),
                                result.getStatus().bidResultStatus, result.getCurrentBidder())
                    );
        });
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

    @Override
    public Topic<BidEvent> bidEvents() {
        return TopicProducer.taggedStreamWithOffset(AuctionEvent.TAGS, this::streamForTag);
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
        return new Bid(bid.getBidder(), bid.getBidTime(), bid.getBidPrice(), bid.getMaximumBid());
    }

    private PersistentEntityRef<AuctionCommand> entityRef(UUID itemId) {
        return registry.refFor(AuctionEntity.class, itemId.toString());
    }
}
