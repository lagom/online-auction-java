package com.example.auction.transaction.impl;

import akka.Done;
import akka.NotUsed;
import com.example.auction.item.api.*;
import com.example.auction.pagination.PaginatedSequence;
import com.example.auction.transaction.api.DeliveryInfo;
import com.example.auction.transaction.api.TransactionInfo;
import com.example.auction.transaction.api.TransactionInfoStatus;
import com.example.auction.transaction.api.TransactionService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.testkit.ProducerStub;
import com.lightbend.lagom.javadsl.testkit.ProducerStubFactory;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.example.auction.security.ClientSecurity.authenticate;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.bind;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.eventually;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

public class TransactionServiceImplIntegrationTest {

    private final static ServiceTest.Setup setup = defaultSetup().withCassandra(true)
            .configureBuilder(b ->
                    b.configure("cassandra-query-journal.eventual-consistency-delay", "0")
                            .overrides(bind(ItemService.class).to(ItemStub.class))
            );

    private static ServiceTest.TestServer testServer;
    private static TransactionService transactionService;

    @BeforeClass
    public static void beforeAll() {
        testServer = ServiceTest.startServer(setup);
        transactionService = testServer.client(TransactionService.class);
    }

    @AfterClass
    public static void afterAll() {
        testServer.stop();
    }

    private static ProducerStub<ItemEvent> itemProducerStub;

    private final UUID itemId = UUID.randomUUID();
    private final UUID creatorId = UUID.randomUUID();
    private final UUID winnerId = UUID.randomUUID();
    private final ItemData itemData = new ItemData("title", "desc", "EUR", 1, 10, Duration.ofMinutes(10), Optional.empty());
    private final Item item = new Item(itemId, creatorId, itemData, 5000, ItemStatus.COMPLETED, Optional.of(Instant.now()), Optional.of(Instant.now()), Optional.of(winnerId));
    private final ItemEvent.AuctionFinished auctionFinished = new ItemEvent.AuctionFinished(itemId, item);

    private final DeliveryInfo deliveryInfo = new DeliveryInfo("ADDR1", "ADDR2", "CITY", "STATE", 27, "COUNTRY");

    private final TransactionInfo transactionInfoStarted = new TransactionInfo(itemId, creatorId, winnerId, itemData, item.getPrice(), 0, Optional.empty(), TransactionInfoStatus.NEGOTIATING_DELIVERY);
    private final TransactionInfo transactionInfoWithDelivery = new TransactionInfo(itemId, creatorId, winnerId, itemData, item.getPrice(), 0, Optional.of(deliveryInfo), TransactionInfoStatus.NEGOTIATING_DELIVERY);


    @Test
    public void shouldCreateTransactionOnAuctionFinished() {
        itemProducerStub.send(auctionFinished);

        eventually(new FiniteDuration(10, SECONDS), () -> {
            TransactionInfo retrievedTransaction = transactionService.getTransaction(itemId)
                    .handleRequestHeader(authenticate(creatorId))
                    .invoke()
                    .toCompletableFuture()
                    .get(5, SECONDS);
            assertEquals(retrievedTransaction, transactionInfoStarted);
        });
    }

    @Test(expected = NotFound.class)
    public void shouldNotCreateTransactionWithNoWinner() throws Throwable {
        UUID itemIdWithNoWinner = UUID.randomUUID();
        Item itemWithNoWinner = new Item(itemIdWithNoWinner, creatorId, itemData, 5000, ItemStatus.COMPLETED, Optional.of(Instant.now()), Optional.of(Instant.now()), Optional.empty());
        ItemEvent.AuctionFinished auctionFinishedWithNoWinner = new ItemEvent.AuctionFinished(itemIdWithNoWinner, itemWithNoWinner);
        itemProducerStub.send(auctionFinishedWithNoWinner);

        try {
            transactionService.getTransaction(itemIdWithNoWinner)
                    .handleRequestHeader(authenticate(creatorId))
                    .invoke()
                    .toCompletableFuture()
                    .get(5, SECONDS);
        }
        catch(ExecutionException re) {
            throw re.getCause();
        }
        catch (InterruptedException | TimeoutException e) {
            throw e;
        }
    }

    @Test
    public void shouldSubmitDeliveryDetails() {
        itemProducerStub.send(auctionFinished);

        eventually(new FiniteDuration(15, SECONDS), () -> {
            transactionService.submitDeliveryDetails(itemId)
                    .handleRequestHeader(authenticate(winnerId))
                    .invoke(deliveryInfo)
                    .toCompletableFuture()
                    .get(5, SECONDS);

            TransactionInfo retrievedTransaction = transactionService.getTransaction(itemId)
                    .handleRequestHeader(authenticate(creatorId))
                    .invoke()
                    .toCompletableFuture()
                    .get(5, SECONDS);

            assertEquals(retrievedTransaction, transactionInfoWithDelivery);
        });
    }

    private static class ItemStub implements ItemService {

        @Inject
        public ItemStub(ProducerStubFactory topicFactory) {
            itemProducerStub = topicFactory.producer(TOPIC_ID);
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
        public ServiceCall<NotUsed, PaginatedSequence<ItemSummary>> getItemsForUser(
                UUID id, ItemStatus status, Optional<Integer> pageNo, Optional<Integer> pageSize) {
            return null;
        }

        @Override
        public Topic<ItemEvent> itemEvents() {
            return itemProducerStub.topic();
        }
    }
}
