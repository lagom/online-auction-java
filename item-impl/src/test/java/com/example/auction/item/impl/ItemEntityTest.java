package com.example.auction.item.impl;

import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.example.auction.item.impl.PItemCommand.*;
import com.example.auction.item.impl.PItemEvent.AuctionFinished;
import com.example.auction.item.impl.PItemEvent.AuctionStarted;
import com.example.auction.item.impl.PItemEvent.ItemCreated;
import com.example.auction.item.impl.PItemEvent.PriceUpdated;
import com.lightbend.lagom.javadsl.api.transport.NotFound;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import org.junit.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ItemEntityTest {


    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    private PersistentEntityTestDriver<PItemCommand, PItemEvent, PItemState> driver ;

    @Before
    public void createTestDriver() {
        driver = new PersistentEntityTestDriver<>(system, new PItemEntity(), itemId.toString());
    }

    @After
    public void issues() {
        if (!driver.getAllIssues().isEmpty()) {
            driver.getAllIssues().forEach(System.out::println);
            fail("There were issues.");
        }
    }

    private UUID itemId = UUID.randomUUID();
    private UUID creatorId = UUID.randomUUID();
    private Optional<UUID> categoryId = Optional.empty();
    private PItemData itemData = new PItemData("title", "desc", "EUR", 1, 10, Duration.ofMinutes(10), categoryId);

    private PItem pItem = new PItem(itemId, creatorId, itemData);

    private PItemCommand createItem = new CreateItem(pItem);
    private PItemCommand startAuction = new StartAuction(creatorId);


    @Test
    public void shouldEmitEventWhenCreatingItem() {
        Outcome<PItemEvent, PItemState> outcome = driver.run(createItem);
        expectEvents(outcome, new ItemCreated(pItem));
        assertEquals(PItemStatus.CREATED, outcome.state().getStatus());
    }

    @Test
    public void shouldEmitEventWhenStartingAnAuction() {
        Outcome<PItemEvent, PItemState> outcome = driver.run(createItem, startAuction);

        Instant startInstant = outcome.state().getItem().get().getAuctionStart().get();
        expectEvents(outcome,
                new ItemCreated(pItem),
                new AuctionStarted(itemId, startInstant));
        assertEquals(PItemStatus.AUCTION, outcome.state().getStatus());
    }

    @Test(expected = UpdateFailureException.class)
    public void shouldForbidCommandWhenStartAuctionIsCommandedByADifferentUser() throws Throwable {
        driver.run(createItem);

        UUID hackerId = UUID.randomUUID();
        PItemCommand invalidStartAuction = new StartAuction(hackerId);
        Outcome<PItemEvent, PItemState> outcome = driver.run(invalidStartAuction);

        expectRethrows(outcome);
    }

    @Test
    public void shouldIgnoreDuplicateStartAuctionCommands() {
        Outcome<PItemEvent, PItemState> outcome = driver.run(createItem, startAuction, startAuction, startAuction);

        Instant startInstant = outcome.state().getItem().get().getAuctionStart().get();
        expectEvents(outcome,
                new ItemCreated(pItem),
                new AuctionStarted(itemId, startInstant));
        assertEquals(PItemStatus.AUCTION, outcome.state().getStatus());
    }

    @Test
    public void shouldEmitEventWhenUpdatingPrice() {
        UpdatePrice updatePrice1 = new UpdatePrice(10);
        UpdatePrice updatePrice2 = new UpdatePrice(20);

        // splitting commands into arrange phase and act phase makes it easier to assert emissions since only
        // the last batch is available in the outcome.
        driver.run(createItem, startAuction); //arrange
        Outcome<PItemEvent, PItemState> outcome = driver.run(updatePrice1, updatePrice2); // act

        expectEvents(outcome,
                new PriceUpdated(itemId, 10),
                new PriceUpdated(itemId, 20)
        );
    }

    @Test
    public void shouldEmitEventWhenUpdatingTheItemBeforeStartingAuction() {
        driver.run(createItem); //arrange

        UpdateItem cmd = editAllFields(pItem);

        Outcome<PItemEvent, PItemState> outcome = driver.run(cmd);
        expectEvents(outcome, new PItemEvent.ItemUpdated(
                itemId,
                creatorId,
                cmd.getItemData(),
                PItemStatus.CREATED));
    }

    @Test(expected = UpdateFailureException.class)
    public void shouldFailWhenUpdatingAnTheItemCreatedBySomeoneElse() throws Throwable {
        driver.run(createItem); //arrange

        UUID hacker = UUID.randomUUID();
        UpdateItem cmd = new UpdateItem(
                hacker,
                new PItemData(
                        itemData.getTitle() + " (edited)",
                        itemData.getDescription() + " (edited)",
                        "CAD",
                        itemData.getIncrement() * 2,
                        itemData.getReservePrice() * 3,
                        itemData.getAuctionDuration().plus(1, ChronoUnit.HOURS),
                        categoryId));

        Outcome<PItemEvent, PItemState> outcome = driver.run(cmd);
        expectRethrows(outcome);
    }

    @Test
    public void shouldEmitEventWhenUpdatingOnlyTheItemDescriptionDuringAuction() throws Throwable {
        driver.run(createItem, startAuction); //arrange

        PItem currentItem = getItem().get();
        String description = "Some new description.";
        PItemData newData = currentItem.withDescription(description).getItemData();
        UpdateItem cmd = new UpdateItem(creatorId, newData);

        Outcome<PItemEvent, PItemState> outcome = driver.run(cmd);
        expectEvents(outcome, new PItemEvent.ItemUpdated(itemId, creatorId, newData, PItemStatus.AUCTION));
    }

    @Test(expected = NotFound.class)
    @Ignore
    public void shouldFailWhenUpdatingAnItemThatDoesntExist() throws Throwable {
        UpdateItem cmd = editAllFields(pItem);

        Outcome<PItemEvent, PItemState> outcome = driver.run(cmd);
        expectRethrows(outcome);
    }

    @Test(expected = UpdateFailureException.class)
    public void shouldForbidEditingAnyFieldThatIsNotDescriptionDuringAuction() throws Throwable {
        driver.run(createItem, startAuction);

        PItem currentPItem = getItem().get();
        UpdateItem cmd = editAllFields(currentPItem);

        Outcome<PItemEvent, PItemState> outcome = driver.run(cmd);
        expectRethrows(outcome);
    }

    @Test(expected = UpdateFailureException.class)
    public void shouldForbidEditingAfterAuction() throws Throwable {
        UUID winner = UUID.randomUUID();
        FinishAuction finish = new FinishAuction(Optional.of(winner), 20);
        driver.run(createItem, startAuction, finish);

        PItem currentPItem = getItem().get();
        PItemData newData = currentPItem.withDescription("new description").getItemData();
        UpdateItem updateItem = new UpdateItem(creatorId, newData);

        Outcome<PItemEvent, PItemState> outcome = driver.run(updateItem);
        expectRethrows(outcome);
    }


    @Test
    public void shouldEmitEventWhenFinishingAuction() {
        UpdatePrice updatePrice1 = new UpdatePrice(10);
        UUID winner = UUID.randomUUID();
        FinishAuction finish = new FinishAuction(Optional.of(winner), 20);

        driver.run(createItem, startAuction);// arrange a state
        Outcome<PItemEvent, PItemState> outcome = driver.run(updatePrice1, finish); // act

        expectEvents(outcome,
                new PriceUpdated(itemId, 10),
                new AuctionFinished(itemId, Optional.of(winner), 20)
        );
        assertEquals(PItemStatus.COMPLETED, outcome.state().getStatus());
    }

    @Test(expected = PersistentEntity.InvalidCommandException.class)
    public void shouldForbidStartingACompletedAuction() throws Throwable {
        UpdatePrice updatePrice1 = new UpdatePrice(10);
        UUID winner = UUID.randomUUID();
        FinishAuction finish = new FinishAuction(Optional.of(winner), 20);
        PItemCommand restart = new StartAuction(creatorId);

        driver.run(createItem, startAuction, updatePrice1, finish);
        Outcome<PItemEvent, PItemState> outcome = driver.run(restart);

        expectRethrows(outcome);
    }

    @Test
    public void shouldReturnDefaultItemBeforeAuction() {
        GetItem getItem = GetItem.INSTANCE;
        Outcome<PItemEvent, PItemState> outcome = driver.run(createItem, getItem);

        assertEquals(Optional.of(pItem), outcome.getReplies().get(1));
    }

    @Test
    public void shouldReturnItemWithCurrentPriceDuringAuction() {
        int latestPrice = 23;
        UpdatePrice updatePrice1 = new UpdatePrice(latestPrice);
        driver.run(createItem, startAuction, updatePrice1);

        Optional<PItem> maybePItem = getItem();

        assertEquals(latestPrice, maybePItem.get().getPrice());
    }

    @Test
    public void shouldReturnItemWithWinningBidAfterAuction() {
        UpdatePrice updatePrice1 = new UpdatePrice(23);
        UUID winner = UUID.randomUUID();
        int winningPrice = 42;
        UpdatePrice updatePrice2 = new UpdatePrice(winningPrice);
        FinishAuction finish = new FinishAuction(Optional.of(winner), winningPrice);
        driver.run(createItem, startAuction, updatePrice1, updatePrice2, finish);

        Optional<PItem> maybePItem = getItem();

        assertEquals(winningPrice, maybePItem.get().getPrice());
    }

    private Optional<PItem> getItem() {
        GetItem getItem = GetItem.INSTANCE;
        Outcome<PItemEvent, PItemState> outcome = driver.run(getItem);
        return (Optional<PItem>) ((PersistentEntityTestDriver.Reply) outcome.sideEffects().get(0)).msg();
    }


    //  ---------------------------------------------------------------------------------------------------------

    private void expectEvents(Outcome<?, ?> outcome, Object... expected) {
        if (!outcome.events().equals(Arrays.asList(expected))) {
            throw new AssertionError("Failed expectation. Expected [" + Arrays.asList(expected) + "] was not equal to [" + outcome.events() + "].");
        }
    }


    // This method rethrows the side effected exception
    private void expectRethrows(Outcome<PItemEvent, PItemState> outcome) throws Throwable {
        PersistentEntityTestDriver.Reply sideEffect = (PersistentEntityTestDriver.Reply) outcome.sideEffects().get(0);
        throw (Throwable) sideEffect.msg();
    }

    // This method returns the side effected exception
    private <E extends Throwable> E expectException(Outcome<PItemEvent, PItemState> outcome) {
        PersistentEntityTestDriver.Reply sideEffect = (PersistentEntityTestDriver.Reply) outcome.sideEffects().get(0);
        return (E) sideEffect.msg();
    }

    /**
     * Edits all the editable fields of the passed in PItemFields.
     */
    private UpdateItem editAllFields(PItem oldPItem) {
        PItemData oldData = oldPItem.getItemData();
        String newCurrency = (oldData.getCurrencyId().equals("USD")) ? "EUR" : "USD";
        return new UpdateItem(
                creatorId,
                new PItemData(
                        oldData.getTitle() + " (edited)",
                        oldData.getDescription() + " (edited)",
                        newCurrency,
                        oldData.getIncrement() * 2,
                        oldData.getReservePrice() * 3,
                        oldData.getAuctionDuration().plus(1, ChronoUnit.HOURS),
                        categoryId)
        );
    }
}
