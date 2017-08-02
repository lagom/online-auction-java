package com.example.auction.user.impl;

import com.example.auction.pagination.PaginatedSequence;
import com.example.auction.user.api.User;
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

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.bind;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static org.junit.Assert.assertEquals;

public class UserRepositoryTest {

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
    private UserRepository userRepository = testServer.injector().instanceOf(UserRepository.class);
    private AtomicInteger offset;

    private final UUID userId = UUID.randomUUID();
    private final String name = "admin";
    private final String email = "admin@gmail.com";
    private final String password = PUserCommand.hashPassword("admin");
    private final Instant createdAt = Instant.now();
    private final PUser userCreated = new PUser(userId, createdAt, name, email, password);


    @Before
    public void restartOffset() {
        offset = new AtomicInteger(1);
    }


    public PaginatedSequence<User> shouldGetUsers() throws InterruptedException, ExecutionException, TimeoutException {
        return Await.result(userRepository.getUsers(0, 10));
    }

    @Test
    public void shouldCreateUser() throws InterruptedException, ExecutionException, TimeoutException {
        feed(new PUserEvent.PUserCreated(userCreated));
        PaginatedSequence<User> users = shouldGetUsers();
        assertEquals(1, users.getCount());
        User expected = new User(userId, createdAt, name, email);
        assertEquals(expected, users.getItems().get(0));
    }

    @Test
    public void shouldPaginateUserRetrieval() throws InterruptedException, ExecutionException, TimeoutException {
        int initialCount = Await.result(userRepository.countUsers());
        int size = 25;

        for (int i = 0; i < size; i++) {
            feed(new PUserEvent.PUserCreated(buildFixture(createdAt, i)));
        }

        PaginatedSequence<User> createdUsers = Await.result(userRepository.getUsers(0, 10));
        assertEquals(size + initialCount, createdUsers.getCount());
        assertEquals(10, createdUsers.getItems().size());
    }

    private PUser buildFixture(Instant createdAt, int i) {

        return new PUser(UUID.randomUUID(), createdAt, name + i, email + i, password);
    }


    private void feed(PUserEvent userEvent) throws InterruptedException, ExecutionException, TimeoutException {
        Await.result(testDriver.feed(userEvent, Offset.sequence(offset.getAndIncrement())));
    }


}


