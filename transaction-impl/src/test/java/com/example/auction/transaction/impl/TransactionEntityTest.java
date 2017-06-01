package com.example.auction.transaction.impl;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import org.junit.*;

import com.example.auction.transaction.impl.TransactionCommand.*;
import com.example.auction.transaction.impl.TransactionEvent.*;
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

    private final Transaction transaction  = new Transaction(itemId, creator, winner, 2000, 50);

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
    public void testStartTransaction() {
        PersistentEntityTestDriver.Outcome<TransactionEvent, TransactionState> outcome = driver.run(new StartTransaction(transaction));

        assertThat(outcome.state().getStatus(), equalTo(TransactionStatus.NEGOTIATING_DELIVERY));
        assertThat(outcome.state().getTransaction(), equalTo(Optional.of(transaction)));
        assertThat(outcome.events(), hasItem(new TransactionStarted(itemId, transaction)));
    }
}
