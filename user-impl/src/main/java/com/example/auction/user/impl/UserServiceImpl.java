package com.example.auction.user.impl;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.persistence.cassandra.query.javadsl.CassandraReadJournal;
import akka.persistence.query.PersistenceQuery;
import akka.persistence.query.javadsl.CurrentPersistenceIdsQuery;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import com.example.auction.user.api.Credential;
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
        registry.register(CredentialEntity.class);
    }

    @Override
    public ServiceCall<User, User> createUser() {
        return user -> {
            UUID uuid = UUID.randomUUID();
            return entityRef(uuid).ask(new UserCommand.CreateUser(user.getName()));
        };
    }

    @Override
    public ServiceCall<Credential, Done> updateCredential() {
        return (credential) -> {
                        return credRef(credential.getUsername()).ask(new CredentialCommand.UpdateCredential(credential.getId(), credential.getUsername(), CredentialCommand.hashPassword(credential.getPassword())));
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
    public ServiceCall<Credential, String> login() {
        return (Credential req) -> {
            return credRef(req.getUsername()).ask(CredentialCommand.Login.INSTANCE)
                    .thenApply(maybeCredential-> {
                        if(maybeCredential.isPresent()) {
                                     if (CredentialCommand.checkPassword(req.getPassword(),maybeCredential.get().getPassword())){
                                         return maybeCredential.get().getId().toString();
                                     } else {
                                         throw new NotFound("Username or password does not match ");
                                     }
                                 } else {
                                     throw new NotFound("User not found");
                                 }

                            }
                    );
        };
    }


    @Override
    public ServiceCall<NotUsed, Done> logout(UUID userId) {
        return req -> {
            return entityRef(userId).ask(UserCommand.GetUser.INSTANCE)
                    .thenApply(maybeCredential-> {
                        return Done.getInstance();
                    });
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

    private PersistentEntityRef<CredentialCommand> credRef(String username) {
               return registry.refFor(CredentialEntity.class, username);
    }
}
