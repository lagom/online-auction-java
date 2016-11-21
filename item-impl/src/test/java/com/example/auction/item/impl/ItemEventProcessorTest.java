package com.example.auction.item.impl;

import com.datastax.driver.core.utils.UUIDs;
import com.example.auction.item.api.ItemStatus;
import com.example.auction.item.api.ItemSummary;
import com.example.auction.item.api.PaginatedSequence;
import com.example.auction.item.impl.testkit.DoNothingTopicFactory;
import com.example.auction.item.impl.testkit.ReadSideTestDriver;
import com.lightbend.lagom.internal.api.broker.TopicFactory;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.bind;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;


public class ItemEventProcessorTest {

  private final static ServiceTest.Setup setup = defaultSetup().withCassandra(true)
      .configureBuilder(b ->
          // by default, cassandra-query-journal delays propagation of events by 10sec. In test we're using
          // a 1 node cluster so this delay is not necessary.
          b.configure("cassandra-query-journal.eventual-consistency-delay", "0")
              .overrides(bind(ReadSide.class).to(ReadSideTestDriver.class),
                  bind(TopicFactory.class).to(DoNothingTopicFactory.class))
      );

  private static ServiceTest.TestServer testServer;


  @Test
  public void shouldCreateAnItem() throws InterruptedException, ExecutionException, TimeoutException {
    ReadSideTestDriver testDriver = testServer.injector().instanceOf(ReadSideTestDriver.class);
    ItemRepository itemRepository = testServer.injector().instanceOf(ItemRepository.class);

    UUID creatorId = UUID.randomUUID();
    UUID itemId = UUIDs.timeBased();
    PItem item = new PItem(itemId, creatorId, "title", "desc", "USD", 10, 100, Duration.ofMinutes(10));
    testDriver.feed(new PItemEvent.ItemCreated(item), Offset.sequence(1)).toCompletableFuture().get(5, SECONDS);

    PaginatedSequence<ItemSummary> items = itemRepository.getItemsForUser(creatorId, ItemStatus.CREATED, 0, 10).toCompletableFuture().get(5, SECONDS);


    assertEquals(1, items.getCount());
    ItemSummary expected =
        new ItemSummary(itemId, item.getTitle(), item.getCurrencyId(), item.getReservePrice(), item.getStatus().toItemStatus());
    assertEquals(expected, items.getItems().get(0));

  }

  //  ---------------------------------------------------------------------------------------------------

  @BeforeClass
  public static void beforeAll() {
    testServer = ServiceTest.startServer(setup);
  }

  @AfterClass
  public static void afterAll() {
    testServer.stop();
  }
}

