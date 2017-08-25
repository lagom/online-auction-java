package com.example.auction.user.api;

import akka.Done;
import akka.NotUsed;
import com.example.auction.pagination.PaginatedSequence;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;

import java.util.Optional;
import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

public interface UserService extends Service {

    ServiceCall<UserRegistration, User> createUser();

    ServiceCall<NotUsed, User> getUser(UUID userId);

    ServiceCall<NotUsed, PaginatedSequence<User>> getUsers(
            Optional<Integer> pageNo, Optional<Integer> pageSize);

    ServiceCall<NotUsed, Done> logout(UUID userId);

    @Override
    default Descriptor descriptor() {
        return named("user").withCalls(
                pathCall("/api/user", this::createUser),
                pathCall("/api/user/:id", this::getUser),
                pathCall("/api/user/logout/:userId", this::logout),
                pathCall("/api/user?pageNo&pageSize", this::getUsers)
        ).withPathParamSerializer(
                UUID.class, PathParamSerializers.required("UUID", UUID::fromString, UUID::toString)
        );
    }
}
