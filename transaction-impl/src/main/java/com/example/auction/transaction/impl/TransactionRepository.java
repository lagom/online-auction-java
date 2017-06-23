package com.example.auction.transaction.impl;

import akka.Done;
import com.datastax.driver.core.*;
import com.datastax.driver.extras.codecs.enums.EnumNameCodec;
import com.example.auction.pagination.PaginatedSequence;
import com.example.auction.transaction.api.TransactionInfoStatus;
import com.example.auction.transaction.api.TransactionSummary;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static com.example.core.CompletionStageUtils.accept;
import static com.example.core.CompletionStageUtils.doAll;

@Singleton
public class TransactionRepository {

    private final CassandraSession session;

    @Inject
    public TransactionRepository(CassandraSession session, ReadSide readSide) {
        this.session = session;
        readSide.register(TransactionEventProcessor.class);
    }

    CompletionStage<PaginatedSequence<TransactionSummary>> getTransactionsForUser(
            UUID userId, TransactionInfoStatus status, int page, int pageSize) {
        return countTransactionSummary(userId, status)
                .thenCompose(
                        count -> {
                            int offset = page * pageSize;
                            int limit = (page + 1) * pageSize;
                            CompletionStage<PSequence<TransactionSummary>> transactions = offset > count ?
                                    CompletableFuture.completedFuture(TreePVector.empty()) :
                                    selectTransactionSummary(userId, status, offset, limit);
                            return transactions.thenApply(seq -> new PaginatedSequence<>(seq, page, pageSize, count));
                        }
                );
    }

    private CompletionStage<Integer> countTransactionSummary(UUID userId, TransactionInfoStatus status) {
        return session
                .selectOne(
                        "SELECT COUNT(*) FROM transactionSummary " +
                                "WHERE (creatorId = ? OR winnerId = ?) AND status = ? " +
                                "ORDER BY itemId DESC",
                        userId,
                        userId,
                        status
                )
                .thenApply(row -> (int) row.get().getLong("count"));
    }

    private CompletionStage<PSequence<TransactionSummary>> selectTransactionSummary(
            UUID userId, TransactionInfoStatus status, long offset, int limit) {
        return session
                .selectAll(
                        "SELECT * FROM transactionSummary " +
                                "WHERE (creatorId = ? OR winnerId = ?) AND status = ? " +
                                "ORDER BY itemId DESC " +
                                "LIMIT ?",
                        userId,
                        userId,
                        status,
                        limit
                )
                .thenApply(List::stream)
                .thenApply(rows -> rows.skip(offset))
                .thenApply(rows -> rows.map(TransactionRepository::toTransactionSummary))
                .thenApply(transactionSummaries -> transactionSummaries.collect(Collectors.toList()))
                .thenApply(TreePVector::from);
    }

    private static TransactionSummary toTransactionSummary(Row transaction) {
        return new TransactionSummary(
                transaction.getUUID("itemId"),
                transaction.getUUID("creatorId"),
                transaction.getUUID("winnerId"),
                transaction.getString("itemTitle"),
                transaction.getString("currencyId"),
                transaction.getInt("itemPrice"),
                transaction.get("status", TransactionInfoStatus.class)
        );
    }

    private static class TransactionEventProcessor extends ReadSideProcessor<TransactionEvent> {
        private final CassandraSession session;
        private final CassandraReadSide readSide;

        private PreparedStatement insertTransactionStatement;

        @Inject
        public TransactionEventProcessor(CassandraSession session, CassandraReadSide readSide) {
            this.session = session;
            this.readSide = readSide;
        }

        @Override
        public ReadSideHandler<TransactionEvent> buildHandler() {
            return readSide.<TransactionEvent>builder("transactionEventOffset")
                    .setGlobalPrepare(this::createTable)
                    .setPrepare(tag -> prepareStatements())
                    .setEventHandler(TransactionEvent.TransactionStarted.class,
                            e -> insertTransaction(e.getItemId(), e.getTransaction()))
                    .build();
        }

        @Override
        public PSequence<AggregateEventTag<TransactionEvent>> aggregateTags() {
            return TransactionEvent.TAG.allTags();
        }

        private CompletionStage<Done> createTable() {
            return session.executeCreateTable(
                    "CREATE TABLE IF NOT EXISTS transactionSummary (" +
                            "itemId timeuuid PRIMARY KEY, " +
                            "creatorId UUID, " +
                            "winnerId UUID, " +
                            "itemTitle text, " +
                            "currencyId text, " +
                            "itemPrice int, " +
                            "status text" +
                            ") " +
                            "WITH CLUSTERING ORDER BY (itemId DESC)"
            );
        }

        private CompletionStage<Done> prepareStatements() {
            return doAll(
                    session.underlying()
                            .thenAccept(s -> registerCodec(s, new EnumNameCodec<>(TransactionInfoStatus.class)))
                            .thenApply(x -> Done.getInstance()),
                    prepareInsertTransactionStatement()
            );
        }

        private CompletionStage<Done> prepareInsertTransactionStatement() {
            return session.
                    prepare("INSERT INTO transactionSummary(" +
                            "itemId timeuuid, " +
                            "creatorId UUID, " +
                            "winnerId UUID, " +
                            "itemTitle text, " +
                            "currencyId text, " +
                            "itemPrice int, " +
                            "status text" +
                            ") VALUES (" +
                            "?, " + // itemId
                            "?, " + // creatorId
                            "?, " + // winnerId
                            "?, " + // itemTitle
                            "?, " + // currencyId
                            "?, " + // itemPrice
                            "?" +   // status
                            ")"
                    )
                    .thenApply(accept(s -> insertTransactionStatement = s));
        }

        private CompletionStage<List<BoundStatement>> insertTransaction(UUID itemId, Transaction transaction) {
            return CassandraReadSide.completedStatements(
                    Arrays.asList(
                        insertTransactionStatement.bind(
                                itemId,
                                transaction.getCreator(),
                                transaction.getWinner(),
                                transaction.getItemData().getTitle(),
                                transaction.getItemData().getCurrencyId(),
                                transaction.getItemPrice(),
                                TransactionInfoStatus.NEGOTIATING_DELIVERY

                        )
                    )
                );
        }

        private void registerCodec(Session session, TypeCodec<?> codec) {
            session.getCluster().getConfiguration().getCodecRegistry().register(codec);
        }
    }
}
