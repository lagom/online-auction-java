package com.example.auction.transaction.impl;

import com.datastax.driver.core.utils.UUIDs;
import com.example.auction.item.api.ItemData;
import com.example.auction.pagination.PaginatedSequence;
import com.example.auction.transaction.api.TransactionInfoStatus;
import com.example.auction.transaction.api.TransactionSummary;
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
        feed(new TransactionEvent.TransactionStarted(itemId, transaction));
        PaginatedSequence<TransactionSummary> transactions = getTransactions(creatorId, TransactionInfoStatus.NEGOTIATING_DELIVERY);
        assertEquals(1, transactions.getCount());
        TransactionSummary expected = new TransactionSummary(itemId, creatorId, winnerId, itemTitle, currencyId, itemPrice, TransactionInfoStatus.NEGOTIATING_DELIVERY);
        assertEquals(expected, transactions.getItems().get(0));
    }

    private PaginatedSequence<TransactionSummary> getTransactions(UUID userId, TransactionInfoStatus transactionStatus) throws InterruptedException, ExecutionException, TimeoutException {
        return Await.result(transactionRepository.getTransactionsForUser(userId, transactionStatus, 0, 10));
    }

    private void feed(TransactionEvent transactionEvent) throws InterruptedException, ExecutionException, TimeoutException {
        Await.result(testDriver.feed(transactionEvent, Offset.sequence(offset.getAndIncrement())));
    }
}