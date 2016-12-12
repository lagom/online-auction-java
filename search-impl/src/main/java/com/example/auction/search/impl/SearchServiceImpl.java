package com.example.auction.search.impl;

import akka.Done;
import akka.stream.javadsl.Flow;
import com.example.auction.bidding.api.BidEvent;
import com.example.auction.bidding.api.BidEvent.BidPlaced;
import com.example.auction.bidding.api.BidEvent.BiddingFinished;
import com.example.auction.bidding.api.BiddingService;
import com.example.auction.item.api.ItemEvent;
import com.example.auction.item.api.ItemEvent.AuctionFinished;
import com.example.auction.item.api.ItemEvent.AuctionStarted;
import com.example.auction.item.api.ItemEvent.ItemUpdated;
import com.example.auction.item.api.ItemService;
import com.example.auction.search.api.SearchItem;
import com.example.auction.search.api.SearchRequest;
import com.example.auction.search.api.SearchResult;
import com.example.auction.search.api.SearchService;
import com.example.elasticsearch.*;
import com.lightbend.lagom.javadsl.api.ServiceCall;
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

    public static final String INDEX_NAME = "auction-items";
    private final Elasticsearch elasticsearch;

    @Inject
    public SearchServiceImpl(
            Elasticsearch elasticsearch,
            ItemService itemService,
            BiddingService biddingService) {
        this.elasticsearch = elasticsearch;

        // TODO: use ES' _bulk API
        itemService.itemEvents().subscribe().atLeastOnce(Flow.<ItemEvent>create().map(this::toDocument).mapAsync(1, this::store));
        biddingService.bidEvents().subscribe().atLeastOnce(Flow.<BidEvent>create().map(this::toDocument).mapAsync(1, this::store));
    }

    @Override
    public ServiceCall<SearchRequest, SearchResult> search(int pageNo, int pageSize) {
        return req -> {
            QueryRoot query = new QueryBuilder(pageNo, pageSize)
                    .withKeywords(req.getKeywords())
                    .withMaxPrice(req.getMaxPrice(), req.getCurrency())
                    .build();
            return elasticsearch.search(INDEX_NAME).invoke(query).thenApply(result -> {
                TreePVector<SearchItem> items = TreePVector.from(
                        result.getIndexedItem()
                                // only return results with user provided data. We may have indexedItem's without
                                // user defined data because sometimes bid service events will arrive before the
                                // item service events.
                                .filter(ii -> ii.getCreatorId().isPresent() &&
                                        ii.getTitle().isPresent() &&
                                        ii.getDescription().isPresent() &&
                                        ii.getCurrencyId().isPresent())
                                .map(this::toApi)
                                .collect(Collectors.toList()));

                return new SearchResult(items, query.getPageSize(), query.getPageNumber(), result.getHits().getTotal());
            });
        };
    }

    // ------------------------------------------------------------------------------------------

    private CompletionStage<Done> store(Optional<IndexedItem> document) {
        return document
                .map(doc -> elasticsearch.updateIndex(INDEX_NAME, doc.getItemId()).invoke(new UpdateIndexItem(doc)))
                .orElse(CompletableFuture.completedFuture(Done.getInstance()));
    }

    private Optional<IndexedItem> toDocument(ItemEvent event) {
        if (event instanceof AuctionStarted) {
            AuctionStarted started = (AuctionStarted) event;
            return Optional.of(IndexedItem.forAuctionStart(started.getItemId(), started.getStartDate(), started.getEndDate()));
        } else if (event instanceof AuctionFinished) {
            AuctionFinished finish = (AuctionFinished) event;
            return Optional.of(IndexedItem.forAuctionFinish(finish.getItemId(), finish.getItem()));
        } else if (event instanceof ItemUpdated) {
            ItemUpdated details = (ItemUpdated) event;
            return Optional.of(IndexedItem.forItemDetails(
                    details.getItemId(),
                    details.getCreator(),
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
                indexedItem.getCreatorId().orElse(UUID.randomUUID()),
                indexedItem.getTitle().get(),
                indexedItem.getDescription().get(),
                indexedItem.getStatus().get().name(),
                indexedItem.getCurrencyId().get(),
                indexedItem.getPrice(),
                indexedItem.getAuctionStart(),
                indexedItem.getAuctionEnd()
        );
    }
}

