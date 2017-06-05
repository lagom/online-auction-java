package com.example.auction.user.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import org.pcollections.PSequence;

import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

public interface UserService extends Service {

    ServiceCall<User, User> createUser();

    ServiceCall<NotUsed, User> getUser(UUID userId);

    // Remove once we have a proper user service
    ServiceCall<NotUsed, PSequence<User>> getUsers();

    // Remove once we have a proper user service
    ServiceCall<Auth, Auth> login();
    ServiceCall<Auth, Auth> updateAuth();

    @Override
    default Descriptor descriptor() {
        return named("user").withCalls(
                pathCall("/api/user", this::createUser),
                pathCall("/api/user/:id", this::getUser),
                pathCall("/api/user", this::getUsers),
                pathCall("/api/user/login", this::login),
                pathCall("/api/user/update-auth", this::updateAuth)
        ).withPathParamSerializer(UUID.class, PathParamSerializers.required("UUID", UUID::fromString, UUID::toString));
    }
}
