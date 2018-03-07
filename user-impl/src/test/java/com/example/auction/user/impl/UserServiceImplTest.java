package com.example.auction.user.impl;

import com.example.auction.user.api.User;
import com.example.auction.user.api.UserLogin;
import com.example.auction.user.api.UserRegistration;
import com.example.auction.user.api.UserService;
import com.example.testkit.Await;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UserServiceImplTest {


    private final static ServiceTest.Setup setup = defaultSetup().withCassandra()
        .configureBuilder(b ->
            // by default, cassandra-query-journal delays propagation of events by 10sec. In test we're using
            // a 1 node cluster so this delay is not necessary.
            b.configure("cassandra-query-journal.eventual-consistency-delay", "0")
        );

    @BeforeClass
    public static void beforeAll() {
        server = ServiceTest.startServer(setup);
        userService = server.client(UserService.class);
    }

    @AfterClass
    public static void afterAll() {
        server.stop();
    }

    private static ServiceTest.TestServer server;

    private static UserService userService;


    @Test
    public void shouldBeAbleToCreateUsers() throws Exception {
        String name = "admin";
        String email = "admin@gmail.com";
        String password = "admin";
        userService = server.client(UserService.class);
        UserRegistration userRegistration = new UserRegistration(name, email, password);
        User createdUser = createNewUser(userRegistration);
        assertEquals(name, createdUser.getName());
        assertEquals(email, createdUser.getEmail());
    }

    @Test
    public void shouldBeAllowLoggingInWithValidCredentials() throws Exception {
        String name = "admin2";
        String email = "admin2@gmail.com";
        String password = "admin2";
        userService = server.client(UserService.class);
        UserRegistration userRegistration = new UserRegistration(name, email, password);
        User createdUser = createNewUser(userRegistration);

        Await.result(userService.login().invoke(new UserLogin(email, password)));
        // no assertion required.
    }

    // TODO: replace with Forbidden.class
    @Test(expected = NotFound.class)
    public void shouldDenyLoggingInWithInalidCredentials() throws Throwable {
        try {
            Await.result(userService.login().invoke(
                new UserLogin("invalid-email@domain.com", "123456"))
            );
        } catch (RuntimeException e) {
            throw e.getCause();
        }
    }

    private User createNewUser(UserRegistration userRegistration) {
        return Await.result(
            userService
                .createUser()
                .invoke(userRegistration)
        );

    }

}
