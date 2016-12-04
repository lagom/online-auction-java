package com.example.auction.search.impl;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Flow;
import com.example.auction.bidding.api.BidEvent;
import com.example.auction.bidding.api.BidEvent.*;
import com.example.auction.bidding.api.BiddingService;
import com.example.auction.item.api.ItemEvent;
import com.example.auction.item.api.ItemEvent.*;
import com.example.auction.item.api.ItemService;
import com.example.auction.search.api.SearchItem;
import com.example.auction.search.api.SearchRequest;
import com.example.auction.search.api.SearchResult;
import com.example.auction.search.api.SearchService;
import com.example.elasticsearch.ElasticSearch;
import com.example.elasticsearch.IndexedItem;
import com.example.elasticsearch.Query;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 *
 */
public class SearchServiceImpl implements SearchService {

    private static final String indexName = "auction-items";
    private final ElasticSearch elasticSearch;

    @Inject
    public SearchServiceImpl(
            ElasticSearch elasticSearch,
            ItemService itemService,
            BiddingService biddingService) {
        this.elasticSearch = elasticSearch;
// TODO: buffer incoming messages before pushing into ES ?
        itemService.itemEvents().subscribe().atLeastOnce(
                Flow.<ItemEvent>create().map(this::toDocument).mapAsync(1, this::store));
        biddingService.bidEvents().subscribe().atLeastOnce(
                Flow.<BidEvent>create().map(this::toDocument).mapAsync(1, this::store));
    }

    @Override
    public ServiceCall<SearchRequest, SearchResult> search() {
        return null;
    }

    @Override
    public ServiceCall<NotUsed, PSequence<SearchItem>> getUserAuctions(UUID userId) {
        return req -> elasticSearch
                .search(indexName)
                .invoke(new Query())
                .thenApply(xs -> xs.stream())
                .thenApply(xs -> xs.map(this::toApi))
                .thenApply(xs -> xs.collect(Collectors.toList()))
                .thenApply(TreePVector::from);
    }

    // ------------------------------------------------------------------------------------------

    private CompletionStage<Done> store(Optional<IndexedItem> document) {
        return document
                .map(doc -> elasticSearch.updateIndex(indexName, doc.getItemId()).invoke(doc))
                .orElse(CompletableFuture.completedFuture(Done.getInstance()));
    }

    private Optional<IndexedItem> toDocument(ItemEvent event) {
        if (event instanceof AuctionStarted) {
            AuctionStarted started = (AuctionStarted) event;
            return Optional.of(IndexedItem.forAuctionStart(started.getItemId(), started.getStartDate(), started.getEndDate()));
        } else if (event instanceof AuctionFinished) {
            AuctionFinished finish = (AuctionFinished) event;
            return Optional.of(IndexedItem.forAuctionFinish(finish.getItemId()));
        } else if (event instanceof ItemUpdated) {
            ItemUpdated details = (ItemUpdated) event;
            return Optional.of(IndexedItem.forItemDetails(
                    details.getItemId(),
                    details.getTitle(),
                    details.getDescription(),
                    details.getItemStatus(),
                    details.getCurrencyId()));
        } else {
            return Optional.empty();
        }
    }

    private Optional<IndexedItem> toDocument(BidEvent event) {
        if (event instanceof BidPlaced) {
            BidPlaced bid = (BidPlaced) event;
            return Optional.of(IndexedItem.forPrice(bid.getItemId(), bid.getBid().getPrice()));
        } else if (event instanceof BiddingFinished) {
            BiddingFinished bid = (BiddingFinished) event;
            return Optional.of(bid.getWinningBid()
                    .map(winning -> IndexedItem.forWinningBid(bid.getItemId(), winning.getPrice(), winning.getBidder()))
                    .orElse(IndexedItem.forPrice(bid.getItemId(), 0)));
        } else {
            return Optional.empty();
        }
    }

    private SearchItem toApi(IndexedItem indexedItem) {
        return new SearchItem(
                indexedItem.getItemId(),
                indexedItem.getCreatorId().get(),
                indexedItem.getTitle().get(),
                indexedItem.getDescription().get(),
                indexedItem.getCurrencyId().get(),
                indexedItem.getPrice(),
                indexedItem.getAuctionStart(),
                indexedItem.getAuctionEnd()
        );
    }
}

