package com.example.auction.transaction.impl;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.example.auction.item.api.ItemData;
import com.lightbend.lagom.javadsl.api.transport.Forbidden;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import org.junit.*;

import com.example.auction.transaction.impl.TransactionCommand.*;
import com.example.auction.transaction.impl.TransactionEvent.*;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TransactionEntityTest {

    private static ActorSystem system;
    private PersistentEntityTestDriver<TransactionCommand, TransactionEvent, TransactionState> driver;

    @BeforeClass
    public static void startActorSystem() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void shutdownActorSystem() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    private final UUID itemId = UUID.randomUUID();
    private final UUID creator = UUID.randomUUID();
    private final UUID winner = UUID.randomUUID();
    private final ItemData itemData = new ItemData("title", "desc", "EUR", 1, 10, Duration.ofMinutes(10), Optional.empty());
    private final DeliveryData deliveryData = new DeliveryData("Addr1", "Addr2", "City", "State", 27, "Country");

    private final Transaction transaction  = new Transaction(itemId, creator, winner, itemData, 2000);

    private final StartTransaction startTransaction = new StartTransaction(transaction);
    private final SubmitDeliveryDetails submitDeliveryDetails = new SubmitDeliveryDetails(winner, deliveryData);
    private final GetTransaction getTransaction = new GetTransaction(creator);

    @Before
    public void createTestDriver() {
        driver = new PersistentEntityTestDriver<>(system, new TransactionEntity(), itemId.toString());
    }

    @After
    public void noIssues() {
        if (!driver.getAllIssues().isEmpty()) {
            driver.getAllIssues().forEach(System.out::println);
            fail("There were issues " + driver.getAllIssues().get(0));
        }
    }

    @Test
    public void shouldEmitEvenWhenCreatingTransaction() {
        Outcome<TransactionEvent, TransactionState> outcome = driver.run(startTransaction);

        assertThat(outcome.state().getStatus(), equalTo(TransactionStatus.NEGOTIATING_DELIVERY));
        assertThat(outcome.state().getTransaction(), equalTo(Optional.of(transaction)));
        assertThat(outcome.events(), hasItem(new TransactionStarted(itemId, transaction)));
    }

    @Test
    public void shouldEmitEventWhenSubmittingDeliveryDetails(){
        driver.run(startTransaction);
        Outcome<TransactionEvent, TransactionState> outcome = driver.run(submitDeliveryDetails);
        assertThat(outcome.state().getStatus(), equalTo(TransactionStatus.NEGOTIATING_DELIVERY));
        assertThat(outcome.events(), hasItem(new DeliveryDetailsSubmitted(itemId, deliveryData)));
    }

    @Test(expected = Forbidden.class)
    @Ignore
    public void shouldForbidSubmittingDeliveryDetailsByNonBuyer() throws Throwable{
        driver.run(startTransaction);
        UUID hacker = UUID.randomUUID();
        SubmitDeliveryDetails invalid = new SubmitDeliveryDetails(hacker, deliveryData);
        Outcome<TransactionEvent, TransactionState> outcome = driver.run(invalid);
        expectRethrows(outcome);
    }

    @Test
    public void shouldAllowSeeTransactionByItemCreator() {
        driver.run(startTransaction);
        Outcome<TransactionEvent, TransactionState> outcome = driver.run(getTransaction);
        assertThat(outcome.getReplies(), hasItem(outcome.state()));
    }

    @Test(expected = Forbidden.class)
    @Ignore
    public void shouldForbidSeeTransactionByNonWinnerNonCreator() throws Throwable{
        driver.run(startTransaction);
        UUID hacker = UUID.randomUUID();
        GetTransaction invalid = new GetTransaction(hacker);
        Outcome<TransactionEvent, TransactionState> outcome = driver.run(invalid);
        expectRethrows(outcome);
    }

    private void expectRethrows(Outcome<TransactionEvent, TransactionState> outcome) throws Throwable {
        PersistentEntityTestDriver.Reply sideEffect = (PersistentEntityTestDriver.Reply) outcome.sideEffects().get(0);
        throw (Throwable) sideEffect.msg();
    }
}

