package com.example.auction.user.impl;

import akka.NotUsed;
import com.example.auction.pagination.PaginatedSequence;
import com.example.auction.user.api.User;
import com.example.auction.user.api.UserRegistration;
import com.example.auction.user.api.UserService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class UserServiceImpl implements UserService {

    private final PersistentEntityRegistry registry;
    private static final Integer DEFAULT_PAGE_SIZE = 10;
    private final UserRepository userRepository;

    @Inject
    public UserServiceImpl(PersistentEntityRegistry registry, UserRepository userRepository) {
        this.registry = registry;
        this.userRepository = userRepository;

        registry.register(PUserEntity.class);
    }

    @Override
    public ServiceCall<UserRegistration, User> createUser() {
        return user -> {
            UUID uuid = UUID.randomUUID();
            Instant createdAt = Instant.now();
            String password = PUserCommand.hashPassword(user.getPassword());
            PUser createdUser = new PUser(uuid, createdAt, user.getName(), user.getEmail(), password);
            return entityRef(uuid)
                    .ask(new PUserCommand.CreatePUser(user.getName(), user.getEmail(), password))
                    .thenApply(done -> Mappers.toApi(createdUser));
        };
    }

    @Override
    public ServiceCall<NotUsed, User> getUser(UUID userId) {
        return req -> entityRef(userId).ask(PUserCommand.GetPUser.INSTANCE).thenApply(maybeUser -> {
            if (maybeUser.isPresent()) {
                return Mappers.toApi(maybeUser.get());
            } else {
                throw new NotFound("user " + userId + " not found");
            }
        });
    }

    @Override
    public ServiceCall<NotUsed, PaginatedSequence<User>> getUsers(Optional<Integer> pageNo, Optional<Integer> pageSize) {
        return req -> userRepository.getUsers(pageNo.orElse(0), pageSize.orElse(DEFAULT_PAGE_SIZE));
    }

    private PersistentEntityRef<PUserCommand> entityRef(UUID id) {
        return entityRef(id.toString());
    }

    private PersistentEntityRef<PUserCommand> entityRef(String id) {
        return registry.refFor(PUserEntity.class, id);
    }
}
