package com.example.auction.bidding.impl;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import com.example.auction.bidding.impl.AuctionCommand.*;
import com.example.auction.bidding.impl.AuctionEvent.*;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AuctionEntityTest {
    private static ActorSystem system;

    @BeforeClass
    public static void startActorSystem() {
        system = ActorSystem.create("HelloWorldTest");
    }

    @AfterClass
    public static void shutdownActorSystem() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    private PersistentEntityTestDriver<AuctionCommand, AuctionEvent, AuctionState> driver;

    private final UUID itemId = UUID.randomUUID();
    private final UUID creator = UUID.randomUUID();
    private final UUID bidder1 = UUID.randomUUID();
    private final UUID bidder2 = UUID.randomUUID();
    private final Auction auction = new Auction(itemId, creator, 2000, 50, Instant.now(),
            Instant.now().plus(7, ChronoUnit.DAYS));

    @Before
    public void createTestDriver() {
        driver = new PersistentEntityTestDriver<>(system, new AuctionEntity(), itemId.toString());
    }

    @After
    public void noIssues() {
        if (!driver.getAllIssues().isEmpty()) {
            driver.getAllIssues().forEach(System.out::println);
            fail("There were issues " + driver.getAllIssues().get(0));
        }
    }

    @Test
    public void testStartAuction() {
        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new StartAuction(auction));

        assertThat(outcome.state().getStatus(), equalTo(AuctionStatus.UNDER_AUCTION));
        assertThat(outcome.state().getAuction(), equalTo(Optional.of(auction)));
        assertThat(outcome.events(), hasItem(new AuctionEvent.AuctionStarted(itemId, auction)));
    }

    @Test
    public void testPlaceFirstBidUnderReserve() {
        driver.run(new StartAuction(auction));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(500, bidder1));

        assertThat(outcome.state().getBiddingHistory(), hasItem(bid(bidder1, 500, 500)));
        assertThat(outcome.events(), hasItem(bidPlaced(bidder1, 500, 500)));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.ACCEPTED_BELOW_RESERVE, 500, bidder1)));
    }

    @Test
    public void testPlaceFirstBidOverReserve() {
        driver.run(new StartAuction(auction));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(2500, bidder1));

        assertThat(outcome.state().getBiddingHistory(), hasItem(bid(bidder1, 2000, 2500)));
        assertThat(outcome.events(), hasItem(bidPlaced(bidder1, 2000, 2500)));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.ACCEPTED, 2000, bidder1)));
    }

    @Test
    public void testPlaceFirstBidEqualReserve() {
        driver.run(new StartAuction(auction));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(2000, bidder1));

        assertThat(outcome.state().getBiddingHistory(), hasItem(bid(bidder1, 2000, 2000)));
        assertThat(outcome.events(), hasItem(bidPlaced(bidder1, 2000, 2000)));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.ACCEPTED, 2000, bidder1)));
    }

    @Test
    public void testPlaceBidUnderCurrentMaximum() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(2500, bidder2));

        assertThat(outcome.events(), allOf(
                hasItem(bidPlaced(bidder2, 2500, 2500)),
                hasItem(bidPlaced(bidder1, 2550, 3000)),
                hasSize(2)
        ));
        assertThat(outcome.state().getBiddingHistory(), hasSize(3));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.ACCEPTED_OUTBID, 2550, bidder1)));
    }

    @Test
    public void testPlaceBidEqualCurrentMaximum() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(3000, bidder2));

        assertThat(outcome.events(), allOf(
                hasItem(bidPlaced(bidder2, 2950, 3000)),
                hasItem(bidPlaced(bidder1, 3000, 3000)),
                hasSize(2)
        ));
        assertThat(outcome.state().getBiddingHistory(), hasSize(3));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.ACCEPTED_OUTBID, 3000, bidder1)));
    }

    @Test
    public void testPlaceBidOverCurrentMaximum() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(4000, bidder2));

        assertThat(outcome.events(), allOf(
                hasItem(bidPlaced(bidder2, 3050, 4000)),
                hasSize(1)
        ));
        assertThat(outcome.state().getBiddingHistory(), hasSize(2));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.ACCEPTED, 3050, bidder2)));
    }

    @Test
    public void testPlaceBidLessThanIncrementOverCurrentMaximum() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(3020, bidder2));

        assertThat(outcome.events(), allOf(
                hasItem(bidPlaced(bidder2, 3020, 3020)),
                hasSize(1)
        ));
        assertThat(outcome.state().getBiddingHistory(), hasSize(2));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.ACCEPTED, 3020, bidder2)));
    }

    @Test
    public void testPlaceBidLessThanCurrentBid() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1), new PlaceBid(2500, bidder2));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(2520, bidder2));

        assertThat(outcome.events(), hasSize(0));
        assertThat(outcome.state().getBiddingHistory(), hasSize(3));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.TOO_LOW, 2550, bidder1)));
    }

    @Test
    public void testPlaceBidLessThanIncrementOverCurrentBid() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1), new PlaceBid(2500, bidder2));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(2570, bidder2));

        assertThat(outcome.events(), hasSize(0));
        assertThat(outcome.state().getBiddingHistory(), hasSize(3));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.TOO_LOW, 2550, bidder1)));
    }

    @Test
    public void testPlaceBidEqualIncrementOverCurrentBid() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1), new PlaceBid(2500, bidder2));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(2600, bidder2));

        assertThat(outcome.events(), allOf(
                hasItem(bidPlaced(bidder2, 2600, 2600)),
                hasItem(bidPlaced(bidder1, 2650, 3000)),
                hasSize(2)
        ));
        assertThat(outcome.state().getBiddingHistory(), hasSize(5));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.ACCEPTED_OUTBID, 2650, bidder1)));
    }

    @Test(expected = BidValidationException.class)
    public void testPlaceBidSeller() {
        driver.run(new StartAuction(auction));

        driver.run(new PlaceBid(2500, creator));
    }

    @Test
    public void testLowerCurrentBid() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(2500, bidder1));

        assertThat(outcome.events(), allOf(
                hasItem(bidPlaced(bidder1, 2000, 2500)),
                hasSize(1)
        ));
        assertThat(outcome.state().getBiddingHistory(), allOf(
                hasItem(bid(bidder1, 2000, 2500)),
                hasSize(1)
        ));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.ACCEPTED, 2000, bidder1)));
    }


    @Test
    public void testLowerCurrentBidToCurrentPrice() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1), new PlaceBid(2500, bidder2));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(2550, bidder1));

        assertThat(outcome.events(), allOf(
                hasItem(bidPlaced(bidder1, 2550, 2550)),
                hasSize(1)
        ));
        assertThat(outcome.state().getBiddingHistory(), allOf(
                hasItem(bid(bidder1, 2000, 3000)),
                hasItem(bid(bidder2, 2500, 2500)),
                hasItem(bid(bidder1, 2550, 2550)),
                hasSize(3)
        ));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.ACCEPTED, 2550, bidder1)));
    }

    @Test
    public void testLowerCurrentBidBelowCurrentPrice() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1), new PlaceBid(2500, bidder2));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(2400, bidder1));

        assertThat(outcome.events(), hasSize(0));
        assertThat(outcome.state().getBiddingHistory(), hasSize(3));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.TOO_LOW, 2550, bidder1)));
    }

    @Test
    public void testRaiseCurrentBid() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(3500, bidder1));

        assertThat(outcome.events(), allOf(
                hasItem(bidPlaced(bidder1, 2000, 3500)),
                hasSize(1)
        ));
        assertThat(outcome.state().getBiddingHistory(), allOf(
                hasItem(bid(bidder1, 2000, 3500)),
                hasSize(1)
        ));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.ACCEPTED, 2000, bidder1)));
    }

    @Test
    public void testBidAfterAuctionEndTime() {
        Auction auction = new Auction(itemId, creator, 2000, 50, Instant.now(),
                Instant.now().minus(7, ChronoUnit.DAYS));
        driver.run(new StartAuction(auction));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(3000, bidder1));

        assertThat(outcome.events(), hasSize(0));
        assertThat(outcome.state().getBiddingHistory(), hasSize(0));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.FINISHED, 0, null)));
    }
    
    @Test
    public void testFinishAuction() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1), new PlaceBid(3500, bidder2));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(FinishBidding.INSTANCE);
        
        assertThat(outcome.events(), allOf(
                hasSize(1),
                hasItem(new BiddingFinished(itemId))
        ));
        assertThat(outcome.state().getStatus(), equalTo(AuctionStatus.COMPLETE));
    }

    @Test
    public void testCancelAuction() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1), new PlaceBid(3500, bidder2));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(CancelAuction.INSTANCE);

        assertThat(outcome.events(), allOf(
                hasSize(1),
                hasItem(new AuctionCancelled(itemId))
        ));
        assertThat(outcome.state().getStatus(), equalTo(AuctionStatus.CANCELLED));
    }

    @Test
    public void testCancelAuctionBeforeStarted() {
        Outcome<AuctionEvent, AuctionState> outcome1 = driver.run(CancelAuction.INSTANCE);

        assertThat(outcome1.events(), allOf(
                hasSize(1),
                hasItem(new AuctionCancelled(itemId))
        ));
        assertThat(outcome1.state().getStatus(), equalTo(AuctionStatus.CANCELLED));

        // You should not be able to start a cancelled auction
        Outcome<AuctionEvent, AuctionState> outcome2 = driver.run(new StartAuction(auction));

        assertThat(outcome2.events(), hasSize(0));
        assertThat(outcome2.state().getStatus(), equalTo(AuctionStatus.CANCELLED));
    }

    @Test
    public void testCancelAuctionAfterFinished() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1), FinishBidding.INSTANCE);

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(CancelAuction.INSTANCE);

        assertThat(outcome.events(), allOf(
                hasSize(1),
                hasItem(new AuctionCancelled(itemId))
        ));
        assertThat(outcome.state().getStatus(), equalTo(AuctionStatus.CANCELLED));
    }

    @Test
    public void testStartAuctionIdempotence() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1), new PlaceBid(3500, bidder2));

        Outcome<AuctionEvent, AuctionState> outcome1 = driver.run(new StartAuction(auction));

        assertThat(outcome1.events(), hasSize(0));
        assertThat(outcome1.state().getStatus(), equalTo(AuctionStatus.UNDER_AUCTION));

        driver.run(FinishBidding.INSTANCE);

        Outcome<AuctionEvent, AuctionState> outcome2 = driver.run(new StartAuction(auction));

        assertThat(outcome2.events(), hasSize(0));
        assertThat(outcome2.state().getStatus(), equalTo(AuctionStatus.COMPLETE));

        driver.run(CancelAuction.INSTANCE);

        Outcome<AuctionEvent, AuctionState> outcome3 = driver.run(new StartAuction(auction));

        assertThat(outcome3.events(), hasSize(0));
        assertThat(outcome3.state().getStatus(), equalTo(AuctionStatus.CANCELLED));
    }

    @Test
    public void testFinishAuctionIdempotence() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1), new PlaceBid(3500, bidder2), FinishBidding.INSTANCE);

        Outcome<AuctionEvent, AuctionState> outcome1 = driver.run(FinishBidding.INSTANCE);

        assertThat(outcome1.events(), hasSize(0));
        assertThat(outcome1.state().getStatus(), equalTo(AuctionStatus.COMPLETE));

        driver.run(CancelAuction.INSTANCE);

        Outcome<AuctionEvent, AuctionState> outcome2 = driver.run(FinishBidding.INSTANCE);

        assertThat(outcome2.events(), hasSize(0));
        assertThat(outcome2.state().getStatus(), equalTo(AuctionStatus.CANCELLED));
    }

    @Test
    public void testCancelAuctionIdempotence() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1), FinishBidding.INSTANCE);

        Outcome<AuctionEvent, AuctionState> outcome1 = driver.run(FinishBidding.INSTANCE);

        assertThat(outcome1.events(), hasSize(0));
        assertThat(outcome1.state().getStatus(), equalTo(AuctionStatus.COMPLETE));

        driver.run(CancelAuction.INSTANCE);

        Outcome<AuctionEvent, AuctionState> outcome2 = driver.run(FinishBidding.INSTANCE);

        assertThat(outcome2.events(), hasSize(0));
        assertThat(outcome2.state().getStatus(), equalTo(AuctionStatus.CANCELLED));
    }

    @Test
    public void testPlaceBidNotStarted() {
        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(3000, bidder1));

        assertThat(outcome.events(), hasSize(0));
        assertThat(outcome.state().getStatus(), equalTo(AuctionStatus.NOT_STARTED));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.NOT_STARTED, 0, null)));
    }

    @Test
    public void testPlaceBidWhenComplete() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1), FinishBidding.INSTANCE);

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(3000, bidder1));

        assertThat(outcome.events(), hasSize(0));
        assertThat(outcome.state().getStatus(), equalTo(AuctionStatus.COMPLETE));
        assertThat(outcome.state().getBiddingHistory(), hasSize(1));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.FINISHED, 2000, bidder1)));
    }

    @Test
    public void testPlaceBidWhenCancelled() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1), CancelAuction.INSTANCE);

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(3000, bidder1));

        assertThat(outcome.events(), hasSize(0));
        assertThat(outcome.state().getStatus(), equalTo(AuctionStatus.CANCELLED));
        assertThat(outcome.state().getBiddingHistory(), hasSize(1));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.CANCELLED, 2000, bidder1)));
    }

    @Test
    public void testGetAuction() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1));

        Outcome<AuctionEvent, AuctionState> outcome = driver.run(GetAuction.INSTANCE);
        assertThat(outcome.events(), hasSize(0));
        assertThat(outcome.getReplies(), hasItem(outcome.state()));
    }

    @Test
    public void testInitializeNotStarted() {
        AuctionState state = AuctionState.notStarted();

        Outcome<AuctionEvent, AuctionState> outcome1 = driver.initialize(Optional.of(state));
        assertThat(outcome1.state(), equalTo(state));

        Outcome<AuctionEvent, AuctionState> outcome2 = driver.run(new PlaceBid(3500, bidder2));

        assertThat(outcome2.getReplies(), hasItem(reply(PlaceBidStatus.NOT_STARTED, 0, null)));
    }

    @Test
    public void testInitializeUnderAuction() {
        AuctionState state = AuctionState.start(auction).bid(new Bid(bidder1, Instant.now(), 2000, 3000));

        Outcome<AuctionEvent, AuctionState> outcome1 = driver.initialize(Optional.of(state));
        assertThat(outcome1.state(), equalTo(state));

        // Check that it is using the under auction behaviour
        Outcome<AuctionEvent, AuctionState> outcome2 = driver.run(new PlaceBid(3500, bidder2));

        assertThat(outcome2.events(), hasItem(bidPlaced(bidder2, 3050, 3500)));
    }

    @Test
    public void testInitializeComplete() {
        AuctionState state = AuctionState.start(auction)
                .bid(new Bid(bidder1, Instant.now(), 2000, 3000))
                .withStatus(AuctionStatus.COMPLETE);

        Outcome<AuctionEvent, AuctionState> outcome1 = driver.initialize(Optional.of(state));
        assertThat(outcome1.state(), equalTo(state));

        Outcome<AuctionEvent, AuctionState> outcome2 = driver.run(new PlaceBid(3500, bidder2));

        assertThat(outcome2.getReplies(), hasItem(reply(PlaceBidStatus.FINISHED, 2000, bidder1)));
    }

    @Test
    public void testInitializeCancelled() {
        AuctionState state = AuctionState.start(auction)
                .bid(new Bid(bidder1, Instant.now(), 2000, 3000))
                .withStatus(AuctionStatus.CANCELLED);

        Outcome<AuctionEvent, AuctionState> outcome1 = driver.initialize(Optional.of(state));
        assertThat(outcome1.state(), equalTo(state));

        Outcome<AuctionEvent, AuctionState> outcome2 = driver.run(new PlaceBid(3500, bidder2));

        assertThat(outcome2.getReplies(), hasItem(reply(PlaceBidStatus.CANCELLED, 2000, bidder1)));
    }


    @Test
    public void testIncrementBidOverReserveFromBellowReserve() {
	driver.run(new StartAuction(auction), new PlaceBid(500, bidder1));
	
	Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(4000, bidder1));

        assertThat(outcome.events(), allOf(
                hasItem(bidPlaced(bidder1, 2000, 4000)),
                hasSize(1)
        ));
        assertThat(outcome.state().getBiddingHistory(), allOf(
                hasItem(bid(bidder1, 2000, 4000)),
                hasSize(1)
        ));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.ACCEPTED, 2000, bidder1)));
    }


    @Test
    public void testIncrementBidBellowReserveFromBellowReserve() {
        driver.run(new StartAuction(auction), new PlaceBid(500, bidder1));
	
	Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(900, bidder1));

        assertThat(outcome.events(), allOf(
                hasItem(bidPlaced(bidder1, 900, 900)),
                hasSize(1)
        ));
        assertThat(outcome.state().getBiddingHistory(), allOf(
                hasItem(bid(bidder1, 900, 900)),
                hasSize(1)
        ));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.ACCEPTED, 900, bidder1)));
    }


    @Test
    public void testIncrementBidOverReserveFromOverReserve() {
        driver.run(new StartAuction(auction), new PlaceBid(3000, bidder1));
	
	Outcome<AuctionEvent, AuctionState> outcome = driver.run(new PlaceBid(4000, bidder1));

        assertThat(outcome.events(), allOf(
                hasItem(bidPlaced(bidder1, 2000, 4000)),
                hasSize(1)
        ));
        assertThat(outcome.state().getBiddingHistory(), allOf(
                hasItem(bid(bidder1, 2000, 4000)),
                hasSize(1)
        ));
        assertThat(outcome.getReplies(), hasItem(reply(PlaceBidStatus.ACCEPTED, 2000, bidder1)));
    }


    private Matcher<Bid> bid(UUID bidder, int bidPrice, int maximumBid) {
        return allOf(
                featureMatcher(Bid::getBidder, equalTo(bidder), "Bid with bidder", "bidder"),
                featureMatcher(Bid::getBidPrice, equalTo(bidPrice), "Bid with bidPrice", "bidPrice"),
                featureMatcher(Bid::getMaximumBid, equalTo(maximumBid), "Bid with maximumPrice", "maximumBid")
        );
    }

    private Matcher<BidPlaced> bidPlaced(UUID bidder, int bidPrice, int maximumBid) {
        return featureMatcher(BidPlaced::getBid, bid(bidder, bidPrice, maximumBid), "BidPlaced with bid", "bid");
    }
    
    private Matcher<PlaceBidResult> reply(PlaceBidStatus status, int currentPrice, UUID bidder) {
        return allOf(
                featureMatcher(PlaceBidResult::getStatus, equalTo(status), "PlaceBidResult with status", "status"),
                featureMatcher(PlaceBidResult::getCurrentPrice, equalTo(currentPrice), "PlaceBidResult with currentPrice", "currentPrice"),
                featureMatcher(PlaceBidResult::getCurrentBidder, equalTo(bidder), "PlaceBidResult with currentBidder", "currentBidder")
        );
    }

    private <T, U> Matcher<T> featureMatcher(Function<T, U> extract, Matcher<? super U> matcher, String desc, String name) {
        return new FeatureMatcher<T, U>(matcher, desc, name) {
            protected U featureValueOf(T actual) {
                return extract.apply(actual);
            }
        };
    }

    private <T> Matcher<Collection<? super T>> hasSize(Matcher<Integer> matcher) {
        return featureMatcher(Collection::size, matcher,  "Collection with size", "size");
    }

    private <T> Matcher<Collection<? super T>> hasSize(int size) {
        return hasSize(equalTo(size));
    }
}
