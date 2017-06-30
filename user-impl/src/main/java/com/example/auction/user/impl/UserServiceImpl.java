package com.example.auction.user.impl;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.persistence.cassandra.query.javadsl.CassandraReadJournal;
import akka.persistence.query.PersistenceQuery;
import akka.persistence.query.javadsl.CurrentPersistenceIdsQuery;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import com.example.auction.user.api.User;
import com.example.auction.user.api.UserRegistration;
import com.example.auction.user.api.UserService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

public class UserServiceImpl implements UserService {

    private final PersistentEntityRegistry registry;
    private final CurrentPersistenceIdsQuery currentIdsQuery;
    private final Materializer mat;

    @Inject
    public UserServiceImpl(PersistentEntityRegistry registry, ActorSystem system, Materializer mat) {
        this.registry = registry;
        this.mat = mat;
        this.currentIdsQuery =
                PersistenceQuery.get(system).getReadJournalFor(CassandraReadJournal.class, CassandraReadJournal.Identifier());

        registry.register(PUserEntity.class);
    }

    @Override
    public ServiceCall<UserRegistration, User> createUser() {
        return user -> {
            UUID uuid = UUID.randomUUID();
            PUser pUser = new PUser(uuid, user.getName(), user.getEmail());
            return entityRef(uuid)
                    .ask(new PUserCommand.CreatePUser(pUser))
                    .thenApply(done -> Mappers.toApi(pUser));
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
    public ServiceCall<NotUsed, PSequence<User>> getUsers() {
        // Note this should never make production....
        return req -> currentIdsQuery.currentPersistenceIds()
                .filter(id -> id.startsWith("PUserEntity"))
                .mapAsync(4, id -> entityRef(id.substring(11)).ask(PUserCommand.GetPUser.INSTANCE))
                .filter(Optional::isPresent)
                .map(user -> Mappers.toApi(user.get()))
                .runWith(Sink.seq(), mat)
                .thenApply(TreePVector::from);
    }

    private PersistentEntityRef<PUserCommand> entityRef(UUID id) {
        return entityRef(id.toString());
    }

    private PersistentEntityRef<PUserCommand> entityRef(String id) {
        return registry.refFor(PUserEntity.class, id);
    }
}
