package com.example.auction.item.impl;

import akka.Done;
import com.datastax.driver.core.*;
import com.datastax.driver.extras.codecs.enums.EnumNameCodec;
import com.example.auction.item.api.ItemStatus;
import com.example.auction.item.api.ItemSummary;
import com.example.auction.item.api.PaginatedSequence;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static com.example.auction.item.impl.CompletionStageUtils.*;

@Singleton
public class ItemRepository {

    private final CassandraSession session;

    @Inject
    public ItemRepository(CassandraSession session, ReadSide readSide) {
        this.session = session;
        readSide.register(PItemEventProcessor.class);
    }

    CompletionStage<PaginatedSequence<ItemSummary>> getItemsForUser(
            UUID creatorId, ItemStatus status, int page, int pageSize) {
        return countItemsByCreatorInStatus(creatorId, status)
                .thenCompose(
                        count -> {
                            int offset = page * pageSize;
                            int limit = (page + 1) * pageSize;
                            CompletionStage<PSequence<ItemSummary>> items = offset > count ?
                                    CompletableFuture.completedFuture(TreePVector.empty()) :
                                    selectItemsByCreatorInStatus(creatorId, status, offset, limit);
                            return items.thenApply(seq -> new PaginatedSequence<>(seq, page, pageSize, count));
                        }
                );
    }

    private CompletionStage<Integer> countItemsByCreatorInStatus(UUID creatorId, ItemStatus status) {
        return session
                .selectOne(
                        "SELECT COUNT(*) FROM itemSummaryByCreatorAndStatus " +
                                "WHERE creatorId = ? AND status = ? " +
                                "ORDER BY status ASC, itemId DESC",
                        // ORDER BY status is required due to https://issues.apache.org/jira/browse/CASSANDRA-10271
                        creatorId,
                        status
                )
                .thenApply(row -> (int) row.get().getLong("count"));
    }

    private CompletionStage<PSequence<ItemSummary>> selectItemsByCreatorInStatus(
            UUID creatorId, ItemStatus status, long offset, int limit) {
        return session
                .selectAll(
                        "SELECT * FROM itemSummaryByCreatorAndStatus " +
                                "WHERE creatorId = ? AND status = ? " +
                                "ORDER BY status ASC, itemId DESC " +
                                // ORDER BY status is required due to https://issues.apache.org/jira/browse/CASSANDRA-10271
                                "LIMIT ?",
                        creatorId,
                        status,
                        limit
                )
                .thenApply(List::stream)
                .thenApply(rows -> rows.skip(offset))
                .thenApply(rows -> rows.map(ItemRepository::convertItemSummary))
                .thenApply(itemSummaries -> itemSummaries.collect(Collectors.toList()))
                .thenApply(TreePVector::from);
    }

    private static ItemSummary convertItemSummary(Row item) {
        return new ItemSummary(
                item.getUUID("itemId"),
                item.getString("title"),
                item.getString("currencyId"),
                item.getInt("reservePrice"),
                item.get("status", ItemStatus.class)
        );
    }

    private static class PItemEventProcessor extends ReadSideProcessor<PItemEvent> {

        private final CassandraSession session;
        private final CassandraReadSide readSide;

        private PreparedStatement insertItemCreatorStatement;
        private PreparedStatement insertItemSummaryByCreatorStatement;
        private PreparedStatement updateItemSummaryStatement;
        private PreparedStatement updateItemSummaryStatusStatement;

        @Inject
        public PItemEventProcessor(CassandraSession session, CassandraReadSide readSide) {
            this.session = session;
            this.readSide = readSide;
        }

        @Override
        public ReadSideHandler<PItemEvent> buildHandler() {
            return readSide.<PItemEvent>builder("pItemEventOffset")
                    .setGlobalPrepare(this::createTables)
                    .setPrepare(tag -> prepareStatements())
                    .setEventHandler(PItemEvent.ItemCreated.class,
                            e -> insertItem(e.getItem()))
                    .setEventHandler(PItemEvent.ItemUpdated.class,
                            e -> updateItemSummary(e))
                    .setEventHandler(PItemEvent.AuctionStarted.class,
                            e -> updateItemSummaryStatus(e.getItemId(), ItemStatus.AUCTION))
                    .setEventHandler(PItemEvent.AuctionFinished.class,
                            e -> updateItemSummaryStatus(e.getItemId(), ItemStatus.COMPLETED))
                    .build();
        }

        @Override
        public PSequence<AggregateEventTag<PItemEvent>> aggregateTags() {
            return PItemEvent.TAG.allTags();
        }

        private CompletionStage<Done> createTables() {
            return doAll(
                    session.executeCreateTable(
                            "CREATE TABLE IF NOT EXISTS itemCreator (" +
                                    "itemId timeuuid PRIMARY KEY, " +
                                    "creatorId UUID" +
                                    ")"
                    ),
                    session.executeCreateTable(
                            "CREATE TABLE IF NOT EXISTS itemSummaryByCreator (" +
                                    "creatorId UUID, " +
                                    "itemId timeuuid, " +
                                    "title text, " +
                                    "currencyId text, " +
                                    "reservePrice int, " +
                                    "status text, " +
                                    "PRIMARY KEY (creatorId, itemId) " +
                                    ") " +
                                    "WITH CLUSTERING ORDER BY (itemId DESC)"
                    ).thenCompose(done ->
                            session.executeCreateTable(
                                    "CREATE MATERIALIZED VIEW IF NOT EXISTS itemSummaryByCreatorAndStatus AS " +
                                            "SELECT * FROM itemSummaryByCreator " +
                                            "WHERE status IS NOT NULL AND itemId IS NOT NULL " +
                                            "PRIMARY KEY (creatorId, status, itemId) " +
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
                            .thenAccept(s -> registerCodec(s, new EnumNameCodec<>(ItemStatus.class)))
                            .thenApply(x -> Done.getInstance()),
                    prepareInsertItemCreatorStatement(),
                    prepareInsertItemSummaryByCreatorStatement(),
                    prepareUpdateItemStatusStatement(),
                    prepareUpdateItemStatement()
            );
        }

        private CompletionStage<Done> prepareInsertItemCreatorStatement() {
            return session
                    .prepare("INSERT INTO itemCreator(itemId, creatorId) VALUES (?, ?)")
                    .thenApply(accept(s -> insertItemCreatorStatement = s));
        }

        private CompletionStage<Done> prepareInsertItemSummaryByCreatorStatement() {
            return session.
                    prepare("INSERT INTO itemSummaryByCreator(" +
                            "creatorId, " +
                            "itemId, " +
                            "title, " +
                            "currencyId, " +
                            "reservePrice, " +
                            "status" +
                            ") VALUES (" +
                            "?, " + // creatorId
                            "?, " + // itemId
                            "?, " + // title
                            "?, " + // currencyId
                            "?, " + // reservePrice
                            "?" +   // status
                            ")"
                    )
                    .thenApply(accept(s -> insertItemSummaryByCreatorStatement = s));
        }

        private CompletionStage<Done> prepareUpdateItemStatement() {
            return session
                    .prepare("UPDATE itemSummaryByCreator " +
                            "SET title = ? " +
                            ", currencyId = ? " +
                            ", reservePrice = ? " +
                            "WHERE creatorId = ? AND itemId = ?")
                    .thenApply(accept(s -> updateItemSummaryStatement = s));
        }

        private CompletionStage<Done> prepareUpdateItemStatusStatement() {
            return session
                    .prepare("UPDATE itemSummaryByCreator SET status = ? WHERE creatorId = ? AND itemId = ?")
                    .thenApply(accept(s -> updateItemSummaryStatusStatement = s));
        }

        private CompletionStage<List<BoundStatement>> insertItem(PItem item) {
            return completedStatements(
                    insertItemCreator(item),
                    insertItemSummaryByCreator(item)
            );
        }

        private BoundStatement insertItemCreator(PItem item) {
            return insertItemCreatorStatement.bind(
                    item.getId(),
                    item.getCreator()
            );
        }

        private BoundStatement insertItemSummaryByCreator(PItem item) {
            return insertItemSummaryByCreatorStatement.bind(
                    item.getCreator(),
                    item.getId(),
                    item.getItemData().getTitle(),
                    item.getItemData().getCurrencyId(),
                    item.getItemData().getReservePrice(),
                    item.getStatus().toItemStatus()
            );
        }

        private CompletionStage<List<BoundStatement>> updateItemSummary(PItemEvent.ItemUpdated item) {
            return completedStatements(
                    updateItemSummaryStatement.bind(
                            item.getItemDetails().getTitle(),
                            item.getItemDetails().getCurrencyId(),
                            item.getItemDetails().getReservePrice(),
                            item.getCreator(),
                            item.getItemId()));
        }

        private CompletionStage<List<BoundStatement>> updateItemSummaryStatus(UUID itemId, ItemStatus status) {
            return selectItemCreator(itemId)
                    .thenApply(throwIfEmpty(() ->
                            new IllegalStateException("No itemCreator found for itemId " + itemId))
                    )
                    .thenApply(row -> row.getUUID("creatorId"))
                    .thenApply(creatorId -> updateItemSummaryStatusStatement.bind(status, creatorId, itemId))
                    .thenApply(Collections::singletonList);
        }


        private CompletionStage<Optional<Row>> selectItemCreator(UUID itemId) {
            return session.selectOne("SELECT * FROM itemCreator WHERE itemId = ?", itemId);
        }
    }
}
