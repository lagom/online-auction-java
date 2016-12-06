package com.example.auction.item.impl;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.example.auction.bidding.api.*;
import com.example.auction.item.api.*;
import com.example.auction.item.api.Item;
import com.example.auction.item.impl.testkit.Await;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Subscriber;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.TransportException;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.PSequence;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static com.example.auction.security.ClientSecurity.authenticate;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ItemServiceImplIntegrationTest {

    private final static Setup setup = defaultSetup().withCassandra(true)
            .configureBuilder(b ->
                    // by default, cassandra-query-journal delays propagation of events by 10sec. In test we're using
                    // a 1 node cluster so this delay is not necessary.
                    b.configure("cassandra-query-journal.eventual-consistency-delay", "0")
                            .overrides(bind(BiddingService.class).to(BiddingStub.class))

            );

    @BeforeClass
    public static void beforeAll() {
        testServer = ServiceTest.startServer(setup);
        itemService = testServer.client(ItemService.class);
    }

    @AfterClass
    public static void afterAll() {
        testServer.stop();
    }

    private static TestServer testServer;
    private static ItemService itemService;

    /**
     * Stubs BiddingService#Tipc("bidding-BidEvent") so a test can tell messages into this actor to simulate
     * BiddingService behavior.
     */
    private static ActorRef bidEventActor;

    @Test
    public void shouldCreate() {
        UUID creatorId = UUID.randomUUID();
        ItemData createItem = sampleItem();
        Item createdItem = createItem(creatorId, createItem);
        Item retrievedItem = retrieveItem(createdItem);
        assertEquals(createdItem, retrievedItem);
    }


    @Test
    public void shouldReturnAllItemsForTheRequiredUser() {
        ItemData tomItem = sampleItem();
        ItemData jerryItem = sampleItem();
        UUID tom = UUID.randomUUID();
        UUID jerry = UUID.randomUUID();
        createItem(jerry, jerryItem);
        Item createdTomItem = createItem(tom, tomItem);


        eventually(new FiniteDuration(10, SECONDS), () -> {

                    PaginatedSequence<ItemSummary> paginatedSequence = itemService.getItemsForUser(tom, ItemStatus.CREATED, Optional.empty(), Optional.empty()).invoke().toCompletableFuture().get(5, SECONDS);

                    assertEquals(1, paginatedSequence.getCount());
                    ItemSummary expected =
                            new ItemSummary(
                                    createdTomItem.getId(),
                                    createdTomItem.getItemData().getTitle(),
                                    createdTomItem.getItemData().getCurrencyId(),
                                    createdTomItem.getItemData().getReservePrice(),
                                    createdTomItem.getStatus());
                    assertEquals(expected, paginatedSequence.getItems().get(0));
                }
        );
    }

    @Test
    public void shouldUpdatePriceAfterBid() {
        UUID creatorId = UUID.randomUUID();
        ItemData createItem = sampleItem();
        Item createdItem = createItem(creatorId, createItem);
        startAuction(creatorId, createdItem);

        Bid bid = new Bid(UUID.randomUUID(), Instant.now(), 100, 100);
        BidEvent bidEvent = new BidEvent.BidPlaced(createdItem.getId(), bid);
        bidEventActor.tell(bidEvent, ActorRef.noSender());

        eventually(new FiniteDuration(10, SECONDS), () -> {
            Item retrievedItem = retrieveItem(createdItem);
            assertEquals(bid.getPrice(), retrievedItem.getPrice());
        });
    }

    @Test
    public void shouldEditDescriptionDuringAuction() {
        UUID creatorId = UUID.randomUUID();
        ItemData createItem = sampleItem();
        Item createdItem = createItem(creatorId, createItem);
        startAuction(creatorId, createdItem);

        String newDescription = "the new description";
        ItemData newData = createdItem.getItemData().withDescription(newDescription);

        Item retrievedItem = updateItem(createdItem.getId(), creatorId, newData);
        assertEquals(newDescription, retrievedItem.getItemData().getDescription());
    }

    @Test(expected = TransportException.class)
    public void shouldFailEditOnBiddingFinished() throws Throwable {
        UUID creatorId = UUID.randomUUID();
        ItemData createItem = sampleItem();
        Item createdItem = createItem(creatorId, createItem);
        startAuction(creatorId, createdItem);


        UUID bidder1 = UUID.randomUUID();
        Bid bid1 = new Bid(bidder1, Instant.now(), 10, 12);
        BidEvent biddingFinished = new BidEvent.BiddingFinished(createdItem.getId(), Optional.of(bid1));
        bidEventActor.tell(biddingFinished, ActorRef.noSender());

        String newDescription = "the new description";
        ItemData newData = createdItem.getItemData().withDescription(newDescription);


        try {
            updateItem(createdItem.getId(), creatorId, newData);
        } catch (RuntimeException re) {
            throw re // the RuntimeException throw by Await
                    .getCause(); // the TransportException I'm expecting
        }

    }

    @Test
    public void shouldEmitAuctionStartedEvent() {
        UUID creatorId = UUID.randomUUID();
        ItemData createItem = sampleItem();
        Item createdItem = createItem(creatorId, createItem);

        // build the stream and materialize it
        Source<ItemEvent, ?> events = itemService.itemEvents().subscribe().atMostOnceSource();
        CompletionStage<ItemEvent> eventualHead = events
                .dropWhile(event -> !event.getItemId().equals(createdItem.getId()))
                .drop(1) // first event will be the item creation Event.ItemUpdated
                .runWith(Sink.head(), testServer.materializer());

        // cause the event
        startAuction(creatorId, createdItem);

        // result on the stream's eventual head.
        ItemEvent itemEvent = Await.result(eventualHead);
        assertThat(itemEvent, instanceOf(ItemEvent.AuctionStarted.class));
    }

    @Test
    public void shouldEmitItemUpdatedEvent() {
        // build the stream and materialize it
        Source<ItemEvent, ?> events = itemService.itemEvents().subscribe().atMostOnceSource();
        CompletionStage<ItemEvent> emitted = events
                .runWith(Sink.head(), testServer.materializer());

        // cause the event
        UUID creatorId = UUID.randomUUID();
        ItemData createItem = sampleItem();
        Item createdItem = createItem(creatorId, createItem);
        startAuction(creatorId, createdItem);

        ItemEvent result = Await.result(emitted);
        assertThat(result, instanceOf(ItemEvent.ItemUpdated.class));
    }


    @Test
    public void shouldFinishAuctionOnBiddingFinished() {
        UUID creatorId = UUID.randomUUID();
        ItemData createItem = sampleItem();

        Item createdItem = createItem(creatorId, createItem);

        startAuction(creatorId, createdItem);

        UUID bidder1 = UUID.randomUUID();
        Bid bid1 = new Bid(bidder1, Instant.now(), 10, 12);
        BidEvent biddingFinished = new BidEvent.BiddingFinished(createdItem.getId(), Optional.of(bid1));
        bidEventActor.tell(biddingFinished, ActorRef.noSender());

        eventually(new FiniteDuration(10, SECONDS), () -> {
            Item retrievedItem = retrieveItem(createdItem);
            assertEquals(bid1.getPrice(), retrievedItem.getPrice());
        });
    }

    @Test
    public void shouldManageDuplicateBiddingFinishedEventsIdempotently() {

        UUID creatorId = UUID.randomUUID();
        ItemData createItem = sampleItem();
        Item createdItem = createItem(creatorId, createItem);

        startAuction(creatorId, createdItem);

        UUID bidder1 = UUID.randomUUID();
        Bid bid1 = new Bid(bidder1, Instant.now(), 10, 12);
        BidEvent biddingFinished = new BidEvent.BiddingFinished(createdItem.getId(), Optional.of(bid1));
        bidEventActor.tell(biddingFinished, ActorRef.noSender());
        bidEventActor.tell(biddingFinished, ActorRef.noSender());
        bidEventActor.tell(biddingFinished, ActorRef.noSender());

        eventually(new FiniteDuration(10, SECONDS), () -> {
            Item retrievedItem = retrieveItem(createdItem);
            assertEquals(bid1.getPrice(), retrievedItem.getPrice());
        });
    }

    // --------------------------------------------------

    private ItemData sampleItem() {
        return new ItemData("title", "description", "USD", 10, 10, Duration.ofMinutes(10));
    }

    private Item createItem(UUID creatorId, ItemData createItem) {
        return Await.result(itemService.createItem().handleRequestHeader(authenticate(creatorId)).invoke(createItem));
    }

    private Item updateItem(UUID itemId, UUID creatorId, ItemData newItem) {
        return Await.result(itemService.updateItem(itemId).handleRequestHeader(authenticate(creatorId)).invoke(newItem));
    }

    private Item retrieveItem(Item createdItem) {
        return Await.result(itemService.getItem(createdItem.getId()).invoke());
    }

    private Done startAuction(UUID creatorId, Item createdItem) {
        return Await.result(itemService.startAuction(createdItem.getId()).handleRequestHeader(authenticate(creatorId)).invoke());
    }

    // --------------------------------------------------

    private static class BiddingStub implements BiddingService {

        private final Materializer materializer;

        @Inject
        BiddingStub(Materializer materializer) {
            this.materializer = materializer;
        }

        @Override
        public ServiceCall<PlaceBid, BidResult> placeBid(UUID itemId) {
            return null;
        }

        @Override
        public ServiceCall<NotUsed, PSequence<Bid>> getBids(UUID itemId) {
            return null;
        }

        @Override
        public Topic<BidEvent> bidEvents() {
            return new Topic<BidEvent>() {
                @Override
                public TopicId topicId() {
                    return null;
                }

                @Override
                public Subscriber<BidEvent> subscribe() {
                    return new Subscriber<BidEvent>() {
                        @Override
                        public Subscriber<BidEvent> withGroupId(String groupId) throws IllegalArgumentException {
                            return null;
                        }

                        @Override
                        public Source<BidEvent, ?> atMostOnceSource() {
                            return null;
                        }

                        @Override
                        public CompletionStage<Done> atLeastOnce(Flow<BidEvent, Done, ?> flow) {
                            Pair<ActorRef, CompletionStage<Done>> pair = Source.<BidEvent>actorRef(1, OverflowStrategy.fail())
                                    .via(flow)
                                    .toMat(Sink.ignore(), Keep.both())
                                    .run(materializer);
                            bidEventActor = pair.first();
                            return pair.second();
                        }
                    };
                }
            };
        }
    }
}
