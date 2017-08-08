package com.example.auction.user.impl;

import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.javadsl.Source;
import com.example.auction.pagination.PaginatedSequence;
import com.example.auction.user.api.User;
import com.example.auction.user.api.UserEvent;
import com.example.auction.user.api.UserRegistration;
import com.example.auction.user.api.UserService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
            PUser createdUser = new PUser(uuid,  user.getName(), user.getEmail(), password);
            return entityRef(uuid)
                    .ask(new PUserCommand.CreatePUser(user.getName(), user.getEmail(), password))
                    .thenApply(done -> Mappers.toApi(Optional.ofNullable(createdUser)));
        };
    }

    @Override
    public ServiceCall<NotUsed, User> getUser(UUID userId) {

        return request ->

                entityRef(userId)
                        .ask(PUserCommand.GetPUser.INSTANCE)
                        .thenApply(maybeUser -> {
                            User user = Mappers.toApi(((Optional<PUser>) maybeUser));
                            return user;

                        });

    }
    @Override
    public Topic<UserEvent> userEvents() {
        return TopicProducer.taggedStreamWithOffset(PUserEvent.TAG.allTags(), this::streamForTag);
    }

    private Source<Pair<UserEvent, Offset>, ?> streamForTag(AggregateEventTag<PUserEvent> tag, Offset offset) {
        return registry.eventStream(tag, offset).filter(eventOffset ->
                eventOffset.first() instanceof PUserEvent.PUserCreated
        ).mapAsync(1, eventOffset -> {

                PUserEvent.PUserCreated userCreated = (PUserEvent.PUserCreated) eventOffset.first();
                return CompletableFuture.completedFuture(Pair.create(
                        new UserEvent.PUserCreated(convertUser(userCreated.getUser())),
                        eventOffset.second()
                ));

        });
    }
    private User convertUser(com.example.auction.user.impl.PUser user) {
        return new User(user.getId(),user.getCreatedAt(),user.getName(),user.getEmail());
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
    }}
