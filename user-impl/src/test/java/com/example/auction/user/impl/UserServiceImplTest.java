package com.example.auction.user.impl;

import com.example.auction.user.api.User;
import com.example.auction.user.api.UserRegistration;
import com.example.auction.user.api.UserService;
import org.junit.Test;

import java.util.UUID;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

public class UserServiceImplTest {
    private final UUID id = UUID.randomUUID();
    private final String name = "admin";
    private final String email = "admin@gmail.com";
    private final String password = "admin";


    @Test
    public void shouldBeAbleToCreateUsers() throws Exception {
        withServer(defaultSetup().withCassandra(true), server -> {
            UserService userService = server.client(UserService.class);
            User user = new User(id, name, email);
            UserRegistration userRegistration = new UserRegistration(name, email, password);
            User user1 = userService.createUser().invoke(userRegistration).toCompletableFuture().get(10, SECONDS);
            assertEquals(user.getName(), user1.getName());
            assertEquals(user.getEmail(), user1.getEmail());
            userService.getUser(user1.getId()).invoke().toCompletableFuture().get(10, SECONDS);
        });
    }

}