package com.example.auction.user.impl;

import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.example.auction.user.impl.UserCommand.CreateUser;
import com.example.auction.user.impl.UserEvent.UserCreated;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import org.junit.*;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.*;

public class UserEntityTest {

    private static ActorSystem system;
    private PersistentEntityTestDriver<UserCommand, UserEvent, Optional<PUser>> driver;

    @BeforeClass
    public static void startActorSystem() {
        system = ActorSystem.create("UserEntityTest");
    }

    @AfterClass
    public static void shutdownActorSystem() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    private final UUID id = UUID.randomUUID();
    private final String name = "admin";
    private final String email = "admin@gmail.com";


    private final PUser user = new PUser(id, name, email);


    @Before
    public void createTestDriver() {
        driver = new PersistentEntityTestDriver<>(system, new UserEntity(), id.toString());
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
    Outcome<UserEvent, Optional<PUser>> outcome = driver.run(
            new CreateUser(user));
    assertEquals(user, outcome.getReplies().get(0));
//    assertEquals(name, ((UserCreated) outcome.events().get(0)).getUser().getName());
//    assertEquals(email, ((UserCreated) outcome.events().get(0)).getUser().getEmail());
    assertThat(outcome.events(), hasItem(new UserCreated(user)));
    assertEquals(Collections.emptyList(), driver.getAllIssues());
//    assertThat(outcome.state(), equalTo(Optional.of(user)));

}
//    @Test
//    public void testRejectDuplicateCreate() {
//        Outcome<UserEvent, Optional<PUser>> outcome = driver.run(
//                new CreateUser(user));
//        assertEquals(UserEntity.InvalidCommandException.class, outcome.getReplies().get(0).getClass());
//        assertEquals(Collections.emptyList(), outcome.events());
//        assertEquals(Collections.emptyList(), driver.getAllIssues());
//    }


}
