package com.example.auction.search.impl;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.stream.Materializer;
import com.example.auction.bidding.api.*;
import com.example.auction.item.api.*;
import com.example.auction.search.api.SearchRequest;
import com.example.auction.search.api.SearchResult;
import com.example.auction.search.api.SearchService;
import com.example.elasticsearch.ElasticsearchTestUtils;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.ServiceLocator;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import com.lightbend.lagom.javadsl.testkit.ServiceTest.Setup;
import org.junit.*;
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
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.eventually;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

import com.example.core.*;
import scala.concurrent.duration.FiniteDuration;

// This TestCase puts a lot of pressure on elasticserach destroying and rebuilding indices on each run. Currently all
// tests are ignored but can be run on a freshly installed elasticserach 5.0.x individually.
// This TestCase expects an elasticsearch 5.0.x available on port 9200.
public class SearchServiceImplTest {

    private static final Setup setup = defaultSetup()
            .configureBuilder(b ->
                    b.overrides(
                            bind(BiddingService.class).to(BiddingStub.class),
                            bind(ItemService.class).to(ItemStub.class),
                            bind(ServiceLocator.class).to(InMemServiceLocator.class)
                    )
            );

    @BeforeClass
    public static void beforeAll() {
        testServer = ServiceTest.startServer(setup);

        InMemServiceLocator serviceLocator = (InMemServiceLocator) testServer.injector().instanceOf(ServiceLocator.class);
        serviceLocator.registerService("elastic-search", 9200);
        serviceLocator.registerService("elastic-search-test-utils", 9200);

        searchService = testServer.client(SearchService.class);
    }

    @Before
    public void cleanIndex() {
        try {
            // TODO: locate all documents and delete those instead of dropping the index.
            testServer.client(ElasticsearchTestUtils.class).deleteIndex(SearchServiceImpl.INDEX_NAME).invoke().toCompletableFuture().get(5, SECONDS);
            TimeUnit.SECONDS.sleep(1);
        } catch (Throwable t) {
            // ignore failures
        }
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
    @Ignore
    public void shouldFindAnItemUnderAuction() throws InterruptedException, ExecutionException, TimeoutException {
        UUID itemId1 = UUID.randomUUID();
        UUID itemId2 = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        int reservePrice = 23;
        int increment = 2;

        updateItem(itemId1, creatorId);
        updateItem(itemId2, creatorId);
        // only started auctions are returned in search Ops.
        startAuction(itemId1, creatorId, reservePrice, increment);

        assertCountOfResults(1, itemId1);
    }


    @Test
    @Ignore
    public void shouldFilterItemsUnderMaxPrice() throws InterruptedException {
        UUID itemId1 = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID bidder1 = UUID.randomUUID();

        int reservePrice = 23;
        int increment = 2;
        int price = reservePrice + increment;
        int maximumPrice = reservePrice * 10;
        String currency = "EUR";

        // create an item and start it's auction
        updateItem(itemId1, creatorId, currency);
        startAuction(itemId1, creatorId, reservePrice, increment);


        TimeUnit.SECONDS.sleep(1);
        // await until the item is indexed and available.
        assertCountOfResults(1, itemId1);
        // once we grant the item is in the index, then we bid.
        BidEvent.BidPlaced bidPlaced = new BidEvent.BidPlaced(itemId1, new Bid(bidder1, Instant.now().plusMillis(10), price, maximumPrice));
        bidStub.get().tell(bidPlaced, ActorRef.noSender());

        // a high max Price is equivalent to no filtering
        Optional<Integer> oneMillion = Optional.of(100_000_000); // one Million = 100M cents
        SearchRequest search = new SearchRequest(Optional.empty(), oneMillion, Optional.of(currency));
        assertCountOfResults(1, itemId1, search);

        // a very low max Price will block the item
        Optional<Integer> oneCent = Optional.of(1); // one Cent
        search = new SearchRequest(Optional.empty(), oneCent, Optional.of(currency));
        assertCountOfResults(0, itemId1, search);
    }


    @Test
    @Ignore
    public void shouldProvideSameResultsWhenEventsFromBidServiceAndItemServiceArriveUnordered() {
        UUID itemId1 = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID bidder1 = UUID.randomUUID();

        int reservePrice = 23;
        int increment = 2;
        int price = reservePrice + increment;
        int maximumPrice = reservePrice * 10;
        String currency = "EUR";

        // Bid over an Item that was still not reported from the Item Service
        BidEvent.BidPlaced bidPlaced = new BidEvent.BidPlaced(itemId1, new Bid(bidder1, Instant.now().plusMillis(10), price, maximumPrice));
        bidStub.get().tell(bidPlaced, ActorRef.noSender());

        // Later the events of item creation and auction start arrive.
        updateItem(itemId1, creatorId, currency);
        startAuction(itemId1, creatorId, reservePrice, increment);

        // data is merged.
        assertCountOfResults(1, itemId1);
    }

    // ------------------------------------------------------------------

    private void updateItem(UUID itemId1, UUID creatorId) {
        updateItem(itemId1, creatorId, "EUR");
    }

    private void updateItem(UUID itemId1, UUID creatorId, String currency) {
        ItemEvent.ItemUpdated itemCreated1 = new ItemEvent.ItemUpdated(itemId1, creatorId, "titles", "desc", ItemStatus.CREATED, currency);
        itemStub.get().tell(itemCreated1, ActorRef.noSender());
    }

    private void startAuction(UUID itemId1, UUID creatorId, int reservePrice, int increment) {
        ItemEvent.AuctionStarted auctionStarted1 = new ItemEvent.AuctionStarted(itemId1, creatorId, reservePrice, increment, Instant.now(), Instant.now().plusSeconds(50));
        itemStub.get().tell(auctionStarted1, ActorRef.noSender());
    }

    private void assertCountOfResults(int expectedCount, UUID itemId1) {
        assertCountOfResults(expectedCount, itemId1, new SearchRequest(Optional.empty(), Optional.empty(), Optional.empty()));
    }

    private void assertCountOfResults(int expectedCount, UUID itemId1, SearchRequest request) {
        eventually(new FiniteDuration(10, TimeUnit.SECONDS), new FiniteDuration(1, TimeUnit.SECONDS), () -> {
            SearchResult items = searchService
                    .search(0, 15)
                    .invoke(request)
                    .toCompletableFuture()
                    .get(10, SECONDS);
            assertEquals(expectedCount, items.getItems().size());
            if (expectedCount > 0) {
                assertEquals(itemId1, items.getItems().get(0).getId());
            }
        });
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