package com.example.auction.item.impl;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import com.example.auction.bidding.api.*;
import com.example.auction.item.api.*;
import com.example.auction.security.ClientSecurity;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Subscriber;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.PSequence;
import scala.concurrent.duration.FiniteDuration;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ItemServiceImplIntegrationTest {

  private final static Setup setup = defaultSetup().withCassandra(true)
      .withConfigureBuilder(b ->
          // by default, cassandra-query-journal delays propagation of events by 10sec. In test we're using
          // a 1 node cluster so this delay is not necessary.
          b.configure("cassandra-query-journal.eventual-consistency-delay", "0")
              .overrides(bind(BiddingService.class).to(BiddingStub.class))
      );

  private static TestServer testServer = ServiceTest.startServer(setup);

  @Test
  public void shouldCreate() throws InterruptedException, ExecutionException, TimeoutException {

    UUID creatorId = UUID.randomUUID();
    Item createItem = new Item(creatorId, "title", "description", "USD", 10, 10, Duration.ofMinutes(10));
    Item createdItem =
        testServer.client(ItemService.class).createItem().handleRequestHeader(ClientSecurity.authenticate(creatorId)).invoke(createItem).toCompletableFuture().get(5, TimeUnit.SECONDS);
    Item retrievedItem =
        testServer.client(ItemService.class).getItem(createdItem.getId()).invoke().toCompletableFuture().get(5, TimeUnit.SECONDS);

    assertEquals(createdItem, retrievedItem);
  }

  @Test
  public void shouldReturnAllItemsForTheRequiredUser() throws InterruptedException, ExecutionException, TimeoutException {
    UUID tom = UUID.randomUUID();
    UUID jerry = UUID.randomUUID();
    Item tomItem = new Item(tom, "title", "description", "USD", 10, 10, Duration.ofMinutes(10));
    Item jerryItem = new Item(jerry, "title", "description", "USD", 10, 10, Duration.ofMinutes(10));

    testServer.client(ItemService.class).createItem().handleRequestHeader(ClientSecurity.authenticate(jerry)).invoke(jerryItem).toCompletableFuture().get(5, TimeUnit.SECONDS);
    Item createdTomItem =
        testServer.client(ItemService.class).createItem().handleRequestHeader(ClientSecurity.authenticate(tom)).invoke(tomItem).toCompletableFuture().get(5, TimeUnit.SECONDS);


    eventually(new FiniteDuration(10, TimeUnit.SECONDS), () -> {

          PaginatedSequence<ItemSummary> paginatedSequence = testServer.client(ItemService.class).getItemsForUser(tom, ItemStatus.CREATED, Optional.empty(), Optional.empty()).invoke().toCompletableFuture().get(5, TimeUnit.SECONDS);

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


  // --------------------------------------------------

  @BeforeClass
  public static void beforeAll() {
    testServer = ServiceTest.startServer(setup);
  }

  @AfterClass
  public static void afterAll() {
    testServer.stop();
  }

  static class BiddingStub implements BiddingService {

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
              return null;
            }
          };
        }
      };
    }
  }
}
