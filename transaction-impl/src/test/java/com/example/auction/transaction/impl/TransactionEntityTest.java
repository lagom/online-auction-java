package com.example.auction.transaction.impl;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.example.auction.item.api.ItemData;
import com.example.auction.transaction.impl.TransactionCommand.*;
import com.example.auction.transaction.impl.TransactionEvent.DeliveryDetailsSubmitted;
import com.example.auction.transaction.impl.TransactionEvent.DeliveryPriceUpdated;
import com.example.auction.transaction.impl.TransactionEvent.PaymentDetailsSubmitted;
import com.example.auction.transaction.impl.TransactionEvent.TransactionStarted;
import com.lightbend.lagom.javadsl.api.transport.Forbidden;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import org.junit.*;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
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
    private final int deliveryPrice = 500;
    private final Payment payment = new Payment.Offline("Payment sent via wire transfer");

    private final Transaction transaction  = new Transaction(itemId, creator, winner, itemData, 2000);

    private final StartTransaction startTransaction = new StartTransaction(transaction);
    private final SubmitDeliveryDetails submitDeliveryDetails = new SubmitDeliveryDetails(winner, deliveryData);
    private final SetDeliveryPrice setDeliveryPrice = new SetDeliveryPrice(creator, deliveryPrice);
    private final SubmitPaymentDetails submitPaymentDetails = new SubmitPaymentDetails(winner, payment);
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
    public void shouldEmitEventWhenCreatingTransaction() {
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
    public void shouldForbidSubmittingDeliveryDetailsByNonBuyer() throws Throwable{
        driver.run(startTransaction);
        UUID hacker = UUID.randomUUID();
        SubmitDeliveryDetails invalid = new SubmitDeliveryDetails(hacker, deliveryData);
        driver.run(invalid);
    }

    @Test
    public void shouldAllowSeeTransactionByItemCreator() {
        driver.run(startTransaction);
        Outcome<TransactionEvent, TransactionState> outcome = driver.run(getTransaction);
        assertThat(outcome.getReplies(), hasItem(outcome.state()));
    }

    @Test(expected = Forbidden.class)
    public void shouldForbidSeeTransactionByNonWinnerNonCreator() throws Throwable {
        driver.run(startTransaction);
        UUID hacker = UUID.randomUUID();
        GetTransaction invalid = new GetTransaction(hacker);
        driver.run(invalid);
    }

    @Test
    public void shouldEmitEventWhenSettingDeliveryPrice() {
        driver.run(startTransaction);
        Outcome<TransactionEvent, TransactionState> outcome = driver.run(setDeliveryPrice);
        assertThat(outcome.state().getStatus(), equalTo(TransactionStatus.NEGOTIATING_DELIVERY));
        assertThat(outcome.state().getTransaction().get().getDeliveryPrice().get(), equalTo(deliveryPrice));
        assertThat(outcome.events(), hasItem(new DeliveryPriceUpdated(itemId, deliveryPrice)));
    }

    @Test(expected = Forbidden.class)
    public void shouldForbidSettingDeliveryPriceByNonSeller() {
        driver.run(startTransaction);
        UUID hacker = UUID.randomUUID();
        SetDeliveryPrice invalid = new SetDeliveryPrice(hacker, deliveryPrice);
        driver.run(invalid);
    }

    @Test
    public void shouldEmitEventWhenSubmittingPaymentDetails() {
        driver.run(startTransaction);
        Outcome<TransactionEvent, TransactionState> outcome = driver.run(submitPaymentDetails);
        assertThat(outcome.state().getStatus(), equalTo(TransactionStatus.PAYMENT_SUBMITTED));
        assertThat(outcome.state().getTransaction().get().getPayment().get(), equalTo(payment));
        assertThat(outcome.events(), hasItem(new PaymentDetailsSubmitted(itemId, payment)));
    }

    @Test(expected = Forbidden.class)
    public void shouldForbidSubmittingPaymentDetailsByNonBuyer() {
        driver.run(startTransaction);
        UUID hacker = UUID.randomUUID();
        SubmitPaymentDetails invalid = new SubmitPaymentDetails(hacker, payment);
        driver.run(invalid);
    }
}