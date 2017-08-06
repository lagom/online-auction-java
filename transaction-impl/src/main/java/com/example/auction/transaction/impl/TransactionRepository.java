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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static com.example.core.CompletionStageUtils.accept;
import static com.example.core.CompletionStageUtils.doAll;
import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.completedStatements;

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
        return countUserTransactions(userId, status)
                .thenCompose(
                        count -> {
                            int offset = page * pageSize;
                            int limit = (page + 1) * pageSize;
                            CompletionStage<PSequence<TransactionSummary>> transactions = offset > count ?
                                    CompletableFuture.completedFuture(TreePVector.empty()) :
                                    selectUserTransactions(userId, status, offset, limit);
                            return transactions.thenApply(seq -> new PaginatedSequence<>(seq, page, pageSize, count));
                        }
                );
    }

    private CompletionStage<Integer> countUserTransactions(UUID userId, TransactionInfoStatus status) {
        return session
                .selectOne(
                        "SELECT COUNT(*) FROM transactionSummaryByUserAndStatus " +
                                "WHERE userId = ? AND status = ? " +
                                "ORDER BY status ASC, itemId DESC",
                        userId,
                        status
                )
                .thenApply(row -> (int) row.get().getLong("count"));
    }

    private CompletionStage<PSequence<TransactionSummary>> selectUserTransactions(
            UUID userId, TransactionInfoStatus status, long offset, int limit) {
        return session
                .selectAll(
                        "SELECT * FROM transactionSummaryByUserAndStatus " +
                                "WHERE userId = ? AND status = ? " +
                                "ORDER BY status ASC, itemId DESC " +
                                "LIMIT ?",
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

        private PreparedStatement insertTransactionUserStatement;
        private PreparedStatement insertTransactionSummaryByUserStatement;
        private PreparedStatement updateTransactionSummaryStatusStatement;

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
                    .setEventHandler(TransactionEvent.DeliveryDetailsApproved.class,
                            e -> updateTransactionSummaryStatus(e.getItemId(), TransactionInfoStatus.PAYMENT_PENDING))
                    .build();
        }

        @Override
        public PSequence<AggregateEventTag<TransactionEvent>> aggregateTags() {
            return TransactionEvent.TAG.allTags();
        }

        private CompletionStage<Done> createTable() {
            return doAll(
                    session.executeCreateTable(
                            "CREATE TABLE IF NOT EXISTS transactionUser (" +
                                    "itemId timeuuid PRIMARY KEY, " +
                                    "userId UUID" +
                                    ")"
                    ),
                    session.executeCreateTable(
                            "CREATE TABLE IF NOT EXISTS transactionSummaryByUser (" +
                                    "userId UUID, " +
                                    "itemId timeuuid, " +
                                    "creatorId UUID, " +
                                    "winnerId UUID, " +
                                    "itemTitle text, " +
                                    "currencyId text, " +
                                    "itemPrice int, " +
                                    "status text, " +
                                    "PRIMARY KEY (userId, itemId)" +
                                    ") " +
                                    "WITH CLUSTERING ORDER BY (itemId DESC)"
                    ).thenCompose(done ->
                            session.executeCreateTable(
                                    "CREATE MATERIALIZED VIEW IF NOT EXISTS transactionSummaryByUserAndStatus AS " +
                                            "SELECT * FROM transactionSummaryByUser " +
                                            "WHERE status IS NOT NULL AND itemId IS NOT NULL " +
                                            "PRIMARY KEY (userId, status, itemId) " +
                                            "WITH CLUSTERING ORDER BY (status ASC, itemId DESC)"
                            )
                    )
            );
        }

        private void registerCodec(Session session, TypeCodec<?> codec) {
            session.getCluster().getConfiguration().getCodecRegistry().register(codec);
        }

        private CompletionStage<Done> prepareStatements() {
            return doAll(
                    session.underlying()
                            .thenAccept(s -> registerCodec(s, new EnumNameCodec<>(TransactionInfoStatus.class)))
                            .thenApply(x -> Done.getInstance()),
                    prepareInsertTransactionUserStatement(),
                    prepareInsertTransactionSummaryByUserStatement(),
                    prepareUpdateTransactionSummaryStatusStatement()
            );
        }

        private CompletionStage<Done> prepareInsertTransactionUserStatement() {
            return session
                    .prepare("INSERT INTO transactionUser(itemId, userId) VALUES (?, ?)")
                    .thenApply(accept(s -> insertTransactionUserStatement = s));
        }

        private CompletionStage<Done> prepareInsertTransactionSummaryByUserStatement() {
            return session
                    .prepare("INSERT INTO transactionSummaryByUser(" +
                            "userId, " +
                            "itemId, " +
                            "creatorId, " +
                            "winnerId, " +
                            "itemTitle, " +
                            "currencyId, " +
                            "itemPrice, " +
                            "status" +
                            ") VALUES (" +
                            "?, " + // userId
                            "?, " + // itemId
                            "?, " + // creatorId
                            "?, " + // winnerId
                            "?, " + // itemTitle
                            "?, " + // currencyId
                            "?, " + // itemPrice
                            "?" +   // status
                            ")"
                    )
                    .thenApply(accept(s -> insertTransactionSummaryByUserStatement = s));
        }

        private CompletionStage<Done> prepareUpdateTransactionSummaryStatusStatement() {
            return session
                    .prepare("UPDATE transactionSummaryByUser " +
                            "SET status = ? " +
                            "WHERE userId = ? AND itemId = ?"
                    )
                    .thenApply(accept(s -> updateTransactionSummaryStatusStatement = s));
        }

        private CompletionStage<List<BoundStatement>> insertTransaction(UUID itemId, Transaction transaction) {
            return completedStatements(
                    insertTransactionUserStatement.bind(itemId, transaction.getCreator()),
                    insertTransactionSummaryByUser(itemId, transaction.getCreator(), transaction),
                    insertTransactionUserStatement.bind(itemId, transaction.getWinner()),
                    insertTransactionSummaryByUser(itemId, transaction.getWinner(), transaction)
            );
        }

        private BoundStatement insertTransactionSummaryByUser(UUID itemId, UUID userId, Transaction transaction) {
            return insertTransactionSummaryByUserStatement.bind(
                    userId,
                    itemId,
                    transaction.getCreator(),
                    transaction.getWinner(),
                    transaction.getItemData().getTitle(),
                    transaction.getItemData().getCurrencyId(),
                    transaction.getItemPrice(),
                    TransactionInfoStatus.NEGOTIATING_DELIVERY
            );
        }

        private CompletionStage<List<BoundStatement>> updateTransactionSummaryStatus(UUID itemId, TransactionInfoStatus status) {
            return selectTransactionUser(itemId)
                    .thenApply(
                            items -> {
                                if (items.isEmpty())
                                    throw new IllegalStateException("No itemCreator found for itemId " + itemId);
                                else
                                    return items;
                            }
                    )
                    .thenApply(List::stream)
                    .thenApply(rows -> rows.map(row -> row.getUUID("userId")))
                    .thenApply(userIds -> userIds.map(userId -> updateTransactionSummaryStatusStatement.bind(status, userId, itemId)))
                    .thenApply(boundStatement -> boundStatement.collect(Collectors.toList()))
                    .thenCompose(boundStatements -> completedStatements(boundStatements));
        }

        private CompletionStage<List<Row>> selectTransactionUser(UUID itemId) {
            return session.selectAll("SELECT * FROM transactionUser WHERE itemId = ?", itemId);
        }
    }
}