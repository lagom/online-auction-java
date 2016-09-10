package com.example.auction.bidding.impl;

import akka.Done;
import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.*;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PSequence;
import com.example.auction.bidding.impl.AuctionEvent.*;
import com.example.auction.bidding.impl.AuctionCommand.FinishBidding;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * Maintains a read side view of all auctions that gets used to schedule FinishBidding events.
 */
@Singleton
public class AuctionScheduler {

    private final CassandraSession session;
    private final ActorSystem system;
    private final PersistentEntityRegistry registry;
    private final Materializer materializer;
    private final FiniteDuration finishBiddingDelay;

    @Inject
    public AuctionScheduler(CassandraSession session, ActorSystem system, ReadSide readSide, PersistentEntityRegistry registry, Materializer materializer) {
        this.session = session;
        this.system = system;
        this.registry = registry;
        this.materializer = materializer;
        finishBiddingDelay = Duration.create(
                this.system.settings().config().getDuration("auctionSchedulerDelay", TimeUnit.MILLISECONDS),
                TimeUnit.MILLISECONDS);

        readSide.register(AuctionSchedulerProcessor.class);

        system.scheduler().schedule(finishBiddingDelay, finishBiddingDelay,
                this::checkFinishBidding, system.dispatcher());
    }

    private void checkFinishBidding() {
        session.select("SELECT itemId FROM auctionSchedule WHERE endAuction < toTimestamp(now())")
                .runForeach(row -> {
                    UUID uuid = row.getUUID("itemId");
                    registry.refFor(AuctionEntity.class, uuid.toString())
                        .ask(FinishBidding.INSTANCE);
                }, materializer);
    }


    public static class AuctionSchedulerProcessor extends ReadSideProcessor<AuctionEvent> {

        private final CassandraReadSide readSide;
        private final CassandraSession session;

        private PreparedStatement insertAuctionStatement;
        private PreparedStatement deleteAuctionStatement;

        @Inject
        public AuctionSchedulerProcessor(CassandraReadSide readSide, CassandraSession session) {
            this.readSide = readSide;
            this.session = session;
        }

        @Override
        public ReadSideHandler<AuctionEvent> buildHandler() {
            return readSide.<AuctionEvent>builder("auctionSchedulerOffset")
                    .setGlobalPrepare(this::createTable)
                    .setPrepare(tag ->
                        prepareInsertAuctionStatement()
                                .thenCompose(d -> prepareDeleteAuctionStatement())
                    )
                    .setEventHandler(AuctionStarted.class, this::insertAuction)
                    .setEventHandler(BiddingFinished.class, e -> deleteAuction(e.getItemId()))
                    .setEventHandler(AuctionCancelled.class, e -> deleteAuction(e.getItemId()))
                    .build();
        }

        private CompletionStage<Done> createTable() {
            return session.executeCreateTable(
                    "CREATE TABLE IF NOT EXISTS auctionSchedule ( " +
                            "endAuction timestamp, " +
                            "itemId timeuuid, " +
                            "PRIMARY KEY (endAuction, itemId)" +
                    ")");
        }

        private CompletionStage<Done> prepareInsertAuctionStatement() {
            return session.prepare("INSERT INTO auctionSchedule(endAuction, itemId) VALUES (?, ?)")
                    .thenApply(s -> {
                        insertAuctionStatement = s;
                        return Done.getInstance();
                    });
        }

        private CompletionStage<Done> prepareDeleteAuctionStatement() {
            return session.prepare("DELETE FROM auctionSchedule where itemId = ?")
                    .thenApply(s -> {
                        deleteAuctionStatement = s;
                        return Done.getInstance();
                    });
        }

        private CompletionStage<List<BoundStatement>> insertAuction(AuctionStarted started) {
            return completedStatement(insertAuctionStatement.bind(
                    Date.from(started.getAuction().getEndTime()),
                    started.getItemId()
            ));
        }

        private CompletionStage<List<BoundStatement>> deleteAuction(UUID itemId) {
            return completedStatement(deleteAuctionStatement.bind(itemId));
        }

        @Override
        public PSequence<AggregateEventTag<AuctionEvent>> aggregateTags() {
            return AggregateEventTag.shards(AuctionEvent.class, AuctionEvent.NUM_SHARDS);
        }
    }
}
