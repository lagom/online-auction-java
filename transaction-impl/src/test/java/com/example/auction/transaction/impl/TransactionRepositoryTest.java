package com.example.auction.transaction.impl;

import com.datastax.driver.core.utils.UUIDs;
import com.example.auction.item.api.ItemData;
import com.example.auction.pagination.PaginatedSequence;
import com.example.auction.transaction.api.TransactionInfoStatus;
import com.example.auction.transaction.api.TransactionSummary;
import com.example.auction.transaction.impl.TransactionEvent.*;
import com.example.testkit.Await;
import com.example.testkit.DoNothingTopicFactory;
import com.example.testkit.ReadSideTestDriver;
import com.lightbend.lagom.internal.javadsl.api.broker.TopicFactory;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.bind;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static org.junit.Assert.assertEquals;

public class TransactionRepositoryTest {

    private final static ServiceTest.Setup setup = defaultSetup().withCassandra(true)
            .configureBuilder(b ->
                    // by default, cassandra-query-journal delays propagation of events by 10sec. In test we're using
                    // a 1 node cluster so this delay is not necessary.
                    b.configure("cassandra-query-journal.eventual-consistency-delay", "0")
                            .overrides(bind(ReadSide.class).to(ReadSideTestDriver.class),
                                    bind(TopicFactory.class).to(DoNothingTopicFactory.class))
            );

    private static ServiceTest.TestServer testServer;

    @BeforeClass
    public static void beforeAll() {
        testServer = ServiceTest.startServer(setup);
    }

    @AfterClass
    public static void afterAll() {
        testServer.stop();
    }

    private ReadSideTestDriver testDriver = testServer.injector().instanceOf(ReadSideTestDriver.class);
    private TransactionRepository transactionRepository = testServer.injector().instanceOf(TransactionRepository.class);
    private AtomicInteger offset;

    private final UUID itemId = UUIDs.timeBased();
    private final UUID creatorId = UUID.randomUUID();
    private final UUID winnerId = UUID.randomUUID();
    private final String itemTitle = "title";
    private final String currencyId = "EUR";
    private final int itemPrice = 2000;
    private final ItemData itemData = new ItemData(itemTitle, "desc", currencyId, 1, 10, Duration.ofMinutes(10), Optional.empty());
    private final Transaction transaction  = new Transaction(itemId, creatorId, winnerId, itemData, itemPrice);

    @Before
    public void restartOffset() {
        offset = new AtomicInteger(1);
    }

    @Test
    public void shouldGetTransactionStarted() throws InterruptedException, ExecutionException, TimeoutException {
        feed(new TransactionStarted(itemId, transaction));
        PaginatedSequence<TransactionSummary> transactions = getTransactions(creatorId, TransactionInfoStatus.NEGOTIATING_DELIVERY);
        assertEquals(1, transactions.getCount());
        TransactionSummary expected = new TransactionSummary(itemId, creatorId, winnerId, itemTitle, currencyId, itemPrice, TransactionInfoStatus.NEGOTIATING_DELIVERY);
        assertEquals(expected, transactions.getItems().get(0));
    }

    @Test
    public void shouldPaginateTransactionRetrieval() throws InterruptedException, ExecutionException, TimeoutException {
        for (int i = 0; i < 35; i++) {
            UUID itemId = UUIDs.timeBased();
            feed(new TransactionStarted(itemId, buildFixture(itemId, i)));
        }

        PaginatedSequence<TransactionSummary> createdItems = Await.result(transactionRepository.getTransactionsForUser(creatorId, TransactionInfoStatus.NEGOTIATING_DELIVERY, 2, 10));
        assertEquals(35, createdItems.getCount());
        assertEquals(10, createdItems.getItems().size());
        // default ordering is time DESC so page 2 of size 10 over a set of 35 returns item ids 5-14. On that seq, the fifth item is id=10
        assertEquals("title10", createdItems.getItems().get(4).getItemTitle());
    }

    private Transaction buildFixture(UUID itemId, int id) {
        ItemData data = new ItemData("title" + id, "desc" + id, "USD", 10, 100, Duration.ofMinutes(10), Optional.empty());
        return new Transaction(itemId, creatorId, UUID.randomUUID(), data, 2000);
    }

    private PaginatedSequence<TransactionSummary> getTransactions(UUID userId, TransactionInfoStatus transactionStatus) throws InterruptedException, ExecutionException, TimeoutException {
        return Await.result(transactionRepository.getTransactionsForUser(userId, transactionStatus, 0, 10));
    }

    private void feed(TransactionEvent transactionEvent) throws InterruptedException, ExecutionException, TimeoutException {
        Await.result(testDriver.feed(transactionEvent, Offset.sequence(offset.getAndIncrement())));
    }
}