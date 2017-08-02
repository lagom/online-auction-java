package com.example.auction.user.impl;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.extras.codecs.jdk8.InstantCodec;
import com.example.auction.pagination.PaginatedSequence;
import com.example.auction.user.api.User;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static com.example.core.CompletionStageUtils.accept;
import static com.example.core.CompletionStageUtils.doAll;
import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.completedStatements;

@Singleton
public class UserRepository {

    private final CassandraSession session;

    @Inject
    public UserRepository(CassandraSession session, ReadSide readSide) {
        this.session = session;
        readSide.register(PUserEventProcessor.class);
    }

    CompletionStage<PaginatedSequence<User>> getUsers(int page, int pageSize) {
        return countUsers()
                .thenCompose(
                        count -> {
                            int offset = page * pageSize;
                            int limit = (page + 1) * pageSize;
                            CompletionStage<PSequence<User>> Users = offset > count ?
                                    CompletableFuture.completedFuture(TreePVector.empty()) :
                                    selectUsers(offset, limit);
                            return Users.thenApply(seq -> new PaginatedSequence<>(seq, page, pageSize, count));
                        }
                );
    }

    public CompletionStage<Integer> countUsers() {
        return session
                .selectOne(
                        "SELECT COUNT(*) FROM UserInfo  "

                )
                .thenApply(row -> (int) row.get().getLong("count"));
    }

    private CompletionStage<PSequence<User>> selectUsers(long offset, int limit) {

        return session
                .selectAll(
                        "SELECT * FROM UserInfo  " +
                                "LIMIT ?",

                        limit
                )
                .thenApply(List::stream)
                .thenApply(rows -> rows.skip(offset))
                .thenApply(rows -> rows.map(UserRepository::convertUserSummary))
                .thenApply(UserSummaries -> UserSummaries.collect(Collectors.toList()))
                .thenApply(TreePVector::from);
    }


    private static User convertUserSummary(Row user) {
        return new User(

                user.getUUID("userId"),
                user.get("createdAt", InstantCodec.instance),
                user.getString("name"),

                user.getString("email")
        );
    }

    private static class PUserEventProcessor extends ReadSideProcessor<PUserEvent> {

        private final CassandraSession session;
        private final CassandraReadSide readSide;

        private PreparedStatement insertUserStatement;

        @Inject
        public PUserEventProcessor(CassandraSession session, CassandraReadSide readSide) {
            this.session = session;
            this.readSide = readSide;
        }

        @Override
        public ReadSideHandler<PUserEvent> buildHandler() {
            return readSide.<PUserEvent>builder("pUserEventOffset")
                    .setGlobalPrepare(this::createTables)
                    .setPrepare(tag -> prepareStatements())
                    .setEventHandler(PUserEvent.PUserCreated.class,
                            e -> insertUser(e.getUser()))

                    .build();
        }

        private void registerCodec(Session session, InstantCodec codec) {
            session.getCluster().getConfiguration().getCodecRegistry().register(codec);
        }

        @Override
        public PSequence<AggregateEventTag<PUserEvent>> aggregateTags() {
            return PUserEvent.TAG.allTags();
        }

        private CompletionStage<Done> createTables() {
            return doAll(
                    session.executeCreateTable(
                            "CREATE TABLE IF NOT EXISTS UserInfo (" +
                                    "userId UUID, " +
                                    "createdAt timestamp, " +
                                    "name text, " +
                                    "email text, " +
                                    "PRIMARY KEY (userId, createdAt) " +
                                    ")"

                    )

            );
        }

        private CompletionStage<Done> prepareStatements() {
            return doAll(
                    session.underlying()
                            .thenAccept(s -> registerCodec(s, InstantCodec.instance))
                            .thenApply(x -> Done.getInstance()),
                    prepareInsertUserStatement());


        }


        private CompletionStage<Done> prepareInsertUserStatement() {

            return session.
                    prepare("INSERT INTO UserInfo(" +

                            "userId, " +
                            "createdAt, " +
                            "name, " +
                            "email" +

                            ") VALUES (" +
                            "?, " + // userId
                            "?, " + // createdAt
                            "?, " + // name
                            "?" + // email

                            ")"
                    )
                    .thenApply(accept(s -> insertUserStatement = s));
        }


        private CompletionStage<List<BoundStatement>> insertUser(PUser user) {
            return completedStatements(
                    insertUserCreator(user)

            );
        }

        private BoundStatement insertUserCreator(PUser user) {
            return insertUserStatement.bind(

                    user.getId(),
                    user.getCreatedAt(),
                    user.getName(),
                    user.getEmail()
            );
        }

        private CompletionStage<Optional<Row>> selectUser(UUID UserId) {
            return session.selectOne("SELECT * FROM UserInfo WHERE userId = ?", UserId);
        }

    }
}

