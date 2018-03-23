package com.example.auction.user.impl;

import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.example.auction.user.impl.PUserCommand.CreatePUser;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import org.junit.*;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UserEntityTest {

    private static ActorSystem system;
    private PersistentEntityTestDriver<PUserCommand, PUserEvent, Optional<PUser>> driver;

    @BeforeClass
    public static void startActorSystem() {
        system = ActorSystem.create("UserEntityTest");
    }

    @AfterClass
    public static void shutdownActorSystem() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    private final UUID id = UUID.randomUUID();
    private final String name = "admin";
    private final String email = "admin@gmail.com";

    private final String password = PUserEntity.hashPassword("admin");


    private final PUser user = new PUser(id,  name, email, password);


    @Before
    public void createTestDriver() {
        driver = new PersistentEntityTestDriver<>(system, new PUserEntity(), id.toString());
    }

    @After
    public void noIssues() {
        if (!driver.getAllIssues().isEmpty()) {
            driver.getAllIssues().forEach(System.out::println);
            fail("There were issues " + driver.getAllIssues().get(0));
        }
    }

    @Test
    public void testCreateUser() {
        Outcome<PUserEvent, Optional<PUser>> outcome = driver.run(
                new CreatePUser(user.getName(), user.getEmail(), user.getPasswordHash()));

        assertEquals(name, ((PUserEvent.PUserCreated) outcome.events().get(0)).getUser().getName());
        assertEquals(email, ((PUserEvent.PUserCreated) outcome.events().get(0)).getUser().getEmail());
        assertEquals(id, ((PUserEvent.PUserCreated) outcome.events().get(0)).getUser().getId());
        assertEquals(password, ((PUserEvent.PUserCreated) outcome.events().get(0)).getUser().getPasswordHash());

        assertEquals(Collections.emptyList(), driver.getAllIssues());
    }

    @Test
    public void testRejectDuplicateCreate() {
        driver.run(new CreatePUser(user.getName(), user.getEmail(), user.getPasswordHash()));
        Outcome<PUserEvent, Optional<PUser>> outcome = driver.run(
                new CreatePUser(user.getName(), user.getEmail(), user.getPasswordHash()));
        assertEquals(PUserEntity.InvalidCommandException.class, outcome.getReplies().get(0).getClass());
        assertEquals(Collections.emptyList(), outcome.events());
        assertEquals(Collections.emptyList(), driver.getAllIssues());
    }

}
