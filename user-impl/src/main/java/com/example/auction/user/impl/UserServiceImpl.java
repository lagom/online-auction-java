package com.example.auction.user.impl;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.persistence.cassandra.query.javadsl.CassandraReadJournal;
import akka.persistence.query.PersistenceQuery;
import akka.persistence.query.javadsl.CurrentPersistenceIdsQuery;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import com.example.auction.user.api.Auth;
import com.example.auction.user.api.User;
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
        registry.register(UserEntity.class);
        registry.register(AuthEntity.class);
    }

    @Override
    public ServiceCall<User, User> createUser() {
        return user -> {
            UUID uuid = UUID.randomUUID();
            return entityRef(uuid).ask(new UserCommand.CreateUser(user.getName()));
        };
    }

    @Override
    public ServiceCall<Auth, Auth> updateAuth() {
        return auth -> {
            return authRef(auth.getUsername()).ask(new AuthCommand.UpdateAuth(auth.getId(), auth.getUsername(), auth.getPassword()));
        };
    }

    @Override
    public ServiceCall<NotUsed, User> getUser(UUID userId) {
        return req -> {
            return entityRef(userId).ask(UserCommand.GetUser.INSTANCE)
                    .thenApply(maybeUser ->
                            maybeUser.orElseGet(() -> {
                                throw new NotFound("User " + userId + " not found");
                            })
                    );
        };
    }

    @Override
    public ServiceCall<Auth, Auth> login() {
        return req -> {
            return authRef(req.getUsername()).ask(AuthCommand.GetAuth.INSTANCE)
                    .thenApply(maybeAuth ->

                            maybeAuth.orElseGet(() -> {
                                throw new NotFound("User not found");
                            })
                    );
        };
    }

    @Override
    public ServiceCall<NotUsed, PSequence<User>> getUsers() {
        // Note this should never make production....
        return req -> currentIdsQuery.currentPersistenceIds()
                .filter(id -> id.startsWith("UserEntity"))
                .mapAsync(4, id -> entityRef(id.substring(10)).ask(UserCommand.GetUser.INSTANCE))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .runWith(Sink.seq(), mat)
                .thenApply(TreePVector::from);
    }


    private PersistentEntityRef<UserCommand> entityRef(UUID id) {
        return entityRef(id.toString());
    }

    private PersistentEntityRef<UserCommand> entityRef(String id) {
            return registry.refFor(UserEntity.class, id);
    }

    private PersistentEntityRef<AuthCommand> authRef(String username) {
               return registry.refFor(AuthEntity.class, username);
    }
}
