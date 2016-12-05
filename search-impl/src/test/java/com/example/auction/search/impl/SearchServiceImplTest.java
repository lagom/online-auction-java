package com.example.auction.search.impl;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.stream.Materializer;
import com.example.auction.bidding.api.*;
import com.example.auction.item.api.*;
import com.example.auction.search.api.SearchItem;
import com.example.auction.search.api.SearchService;
import com.example.auction.search.impl.core.TopicStub;
import com.example.elasticsearch.ElasticSearch;
import com.example.elasticsearch.ElasticSearchInMem;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import com.lightbend.lagom.javadsl.testkit.ServiceTest.Setup;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.PSequence;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.bind;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class SearchServiceImplTest {


    private static final Setup setup = defaultSetup()
            .configureBuilder(b ->
                    b.overrides(
                            bind(BiddingService.class).to(BiddingStub.class),
                            bind(ItemService.class).to(ItemStub.class),
                            bind(ElasticSearch.class).to(ElasticSearchInMem.class)
                    )
            );

    @BeforeClass
    public static void beforeAll() {
        testServer = ServiceTest.startServer(setup);
        searchService = testServer.client(SearchService.class);
    }

    @AfterClass
    public static void afterAll() {
        testServer.stop();
    }

    private static ServiceTest.TestServer testServer;
    private static SearchService searchService;

    private static Supplier<ActorRef> bidStub;
    private static Supplier<ActorRef> itemStub;

    @Test
    public void shouldStoreInfoUpdatedFromBiddingAndItemServices() throws InterruptedException, ExecutionException, TimeoutException {
        UUID itemId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID bidder1 = UUID.randomUUID();

        int reservePrice = 23;
        int increment = 2;
        int price = reservePrice + increment;
        int maximumPrice = reservePrice * 2;

        ItemEvent.ItemUpdated itemCreated = new ItemEvent.ItemUpdated(itemId, creatorId, "titles", "desc", ItemStatus.CREATED, "EUR");
        itemStub.get().tell(itemCreated, ActorRef.noSender());
        ItemEvent.AuctionStarted auctionStarted = new ItemEvent.AuctionStarted(itemId, creatorId, reservePrice, increment, Instant.now(), Instant.now().plusSeconds(50));
        itemStub.get().tell(auctionStarted, ActorRef.noSender());
        BidEvent.BidPlaced bidPlaced = new BidEvent.BidPlaced(itemId, new Bid(bidder1, Instant.now().plusMillis(10), price, maximumPrice));
        bidStub.get().tell(bidPlaced, ActorRef.noSender());

        PSequence<SearchItem> items = searchService
                .getOpenAuctionsUnderPrice(price + 1100)
                .invoke()
                .toCompletableFuture()
                .get(5, TimeUnit.SECONDS);
        assertEquals(itemId, items.get(0).getId());

    }


    // ------------------------------------------------------------------

    public static class ItemStub implements ItemService {

        private final TopicStub<ItemEvent> topicStub;

        @Inject
        public ItemStub(Materializer materializer) {
            topicStub = new TopicStub<>(materializer);
            itemStub = topicStub.actorSupplier();
        }

        @Override
        public Topic<ItemEvent> itemEvents() {
            return topicStub;
        }

        @Override
        public ServiceCall<ItemData, Item> createItem() {
            return null;
        }

        @Override
        public ServiceCall<ItemData, Item> updateItem(UUID id) {
            return null;
        }

        @Override
        public ServiceCall<NotUsed, Done> startAuction(UUID id) {
            return null;
        }

        @Override
        public ServiceCall<NotUsed, Item> getItem(UUID id) {
            return null;
        }

        @Override
        public ServiceCall<NotUsed, PaginatedSequence<ItemSummary>> getItemsForUser(UUID id, ItemStatus status, Optional<Integer> pageNo, Optional<Integer> pageSize) {
            return null;
        }
    }

    // -----------------------------------------------------------------

    public static class BiddingStub implements BiddingService {
        private final TopicStub<BidEvent> topicStub;

        @Inject
        public BiddingStub(Materializer materializer) {
            topicStub = new TopicStub<>(materializer);
            bidStub = topicStub.actorSupplier();
        }


        @Override
        public Topic<BidEvent> bidEvents() {
            return topicStub;
        }

        @Override
        public ServiceCall<PlaceBid, BidResult> placeBid(UUID itemId) {
            return null;
        }

        @Override
        public ServiceCall<NotUsed, PSequence<Bid>> getBids(UUID itemId) {
            return null;
        }
    }


}