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
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Subscriber;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.PSequence;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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

    private static TestServer testServer;
    private static ItemService itemService;

    @Test
    public void shouldCreate() throws InterruptedException, ExecutionException, TimeoutException {

        UUID creatorId = UUID.randomUUID();
        Item createItem = new Item(creatorId, "title", "description", "USD", 10, 10, Duration.ofMinutes(10));
        Item createdItem =
                itemService.createItem().handleRequestHeader(authenticate(creatorId)).invoke(createItem).toCompletableFuture().get(5, SECONDS);
        Item retrievedItem =
                itemService.getItem(createdItem.getId()).invoke().toCompletableFuture().get(5, SECONDS);

        assertEquals(createdItem, retrievedItem);
    }

    @Test
    public void shouldReturnAllItemsForTheRequiredUser() throws InterruptedException, ExecutionException, TimeoutException {
        UUID tom = UUID.randomUUID();
        UUID jerry = UUID.randomUUID();
        Item tomItem = new Item(tom, "title", "description", "USD", 10, 10, Duration.ofMinutes(10));
        Item jerryItem = new Item(jerry, "title", "description", "USD", 10, 10, Duration.ofMinutes(10));

        itemService.createItem().handleRequestHeader(authenticate(jerry)).invoke(jerryItem).toCompletableFuture().get(5, SECONDS);
        Item createdTomItem =
                itemService.createItem().handleRequestHeader(authenticate(tom)).invoke(tomItem).toCompletableFuture().get(5, SECONDS);


        eventually(new FiniteDuration(10, SECONDS), () -> {

                    PaginatedSequence<ItemSummary> paginatedSequence = itemService.getItemsForUser(tom, ItemStatus.CREATED, Optional.empty(), Optional.empty()).invoke().toCompletableFuture().get(5, SECONDS);

                    assertEquals(1, paginatedSequence.getCount());
                    ItemSummary expected =
                            new ItemSummary(
                                    createdTomItem.getId(),
                                    createdTomItem.getTitle(),
                                    createdTomItem.getCurrencyId(),
                                    createdTomItem.getReservePrice(),
                                    createdTomItem.getStatus());
                    assertEquals(expected, paginatedSequence.getItems().get(0));
                }
        );
    }

    @Test
    public void shouldUpdatePriceAfterBid() throws InterruptedException, ExecutionException, TimeoutException {
        UUID creatorId = UUID.randomUUID();
        Item createItem = new Item(creatorId, "title", "description", "USD", 10, 10, Duration.ofMinutes(10));

        Item createdItem =
                itemService.createItem()
                        .handleRequestHeader(authenticate(creatorId))
                        .invoke(createItem)
                        .toCompletableFuture()
                        .get(5, SECONDS);

        itemService.startAuction(createdItem.getId())
                .handleRequestHeader(authenticate(creatorId))
                .invoke()
                .toCompletableFuture()
                .get(5, SECONDS);

        Bid bid = new Bid(UUID.randomUUID(), Instant.now(), 100, 100);
        BidEvent bidEvent = new BidEvent.BidPlaced(createdItem.getId(), bid);
        bidEventActor.tell(bidEvent, ActorRef.noSender());

        eventually(new FiniteDuration(10, SECONDS), () -> {
            Item retrievedItem =
                    itemService.getItem(createdItem.getId()).invoke().toCompletableFuture().get(5, SECONDS);

            assertEquals(bid.getPrice(), retrievedItem.getPrice());
        });
    }

    @Test
    public void shouldEmitAuctionStartedEvent() throws InterruptedException, ExecutionException, TimeoutException {
        UUID creatorId = UUID.randomUUID();
        Item createItem = new Item(creatorId, "title", "description", "USD", 10, 10, Duration.ofMinutes(10));

        Item createdItem =
                itemService.createItem()
                        .handleRequestHeader(authenticate(creatorId))
                        .invoke(createItem)
                        .toCompletableFuture()
                        .get(5, SECONDS);

        itemService.startAuction(createdItem.getId())
                .handleRequestHeader(authenticate(creatorId))
                .invoke()
                .toCompletableFuture()
                .get(5, SECONDS);

        Source<ItemEvent, ?> events = itemService.itemEvents().subscribe().atMostOnceSource();
        ItemEvent itemEvent = events.dropWhile(event -> !event.getItemId().equals(createdItem.getId()))
                .runWith(Sink.head(), testServer.materializer())
                .toCompletableFuture()
                .get(5, SECONDS);

        assertThat(itemEvent, instanceOf(ItemEvent.AuctionStarted.class));
    }


    // --------------------------------------------------

    @BeforeClass
    public static void beforeAll() {
        testServer = ServiceTest.startServer(setup);
        itemService = testServer.client(ItemService.class);
    }

    @AfterClass
    public static void afterAll() {
        testServer.stop();
    }


    private static ActorRef bidEventActor;

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
                            Pair<ActorRef, CompletionStage<Done>> actorAndDone =
                                    Source.<BidEvent>actorRef(1, OverflowStrategy.fail())
                                            .via(flow)
                                            .toMat(Sink.ignore(), Keep.both())
                                            .run(materializer);
                            bidEventActor = actorAndDone.first();
                            return actorAndDone.second();
                        }
                    };
                }
            };
        }
    }
}
