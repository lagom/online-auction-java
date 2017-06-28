package com.example.auction.user.impl;

import com.example.auction.user.api.UserRegistration;
import com.example.auction.user.api.UserService;
import org.junit.Test;

import java.util.UUID;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;

public class UserServiceImplTest {

    private final UUID id1= UUID.randomUUID();
    private final String name = "admin";
    private final String email = "admin@gmail.com";
    private final String password = "admin";


    @Test
    public void shouldBeAbleToCreateUsers() throws Exception {
        withServer(defaultSetup().withCassandra(true), server -> {
            UserService userService = server.client(UserService.class);
            UserRegistration userRegistration= new UserRegistration(name,email,password);
           userService.createUser().invoke(userRegistration).toCompletableFuture().get(10, SECONDS);


        });
    }


}