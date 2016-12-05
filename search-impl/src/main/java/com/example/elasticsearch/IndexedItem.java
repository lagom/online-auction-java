package com.example.elasticsearch;

import com.example.auction.item.api.ItemStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;
import lombok.experimental.Wither;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;


@Value

public class IndexedItem {
    UUID itemId;
    @Wither
    Optional<UUID> creatorId;
    @Wither
    Optional<String> title;
    @Wither
    Optional<String> description;
    @Wither
    Optional<String> currencyId;
    @Wither
    OptionalInt increment;
    @Wither
    OptionalInt price;
    @Wither
    Optional<ItemStatus> status;
    @Wither
    Optional<Instant> auctionStart;
    @Wither
    Optional<Instant> auctionEnd;
    @Wither
    Optional<UUID> winner;
//    private  Optional<UUID> auctionWinner = Optional.empty();
//    private Optional<UUID> categoryId = null;

    @JsonCreator
    private IndexedItem(UUID itemId, Optional<UUID> creatorId, Optional<String> title, Optional<String> description,
                        Optional<String> currencyId, OptionalInt increment, OptionalInt price,
                        Optional<ItemStatus> status, Optional<Instant> auctionStart, Optional<Instant> auctionEnd,
                        Optional<UUID> winner) {
        this.itemId = itemId;
        this.creatorId = creatorId;
        this.title = title;
        this.description = description;
        this.currencyId = currencyId;
        this.increment = increment;
        this.price = price;
        this.status = status;
        this.auctionStart = auctionStart;
        this.auctionEnd = auctionEnd;
        this.winner = winner;
    }

    public IndexedItem(UUID itemId) {
        this(itemId, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), OptionalInt.empty(),
                OptionalInt.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static IndexedItem forPrice(UUID itemId, int price) {
        return new IndexedItem(itemId).withPrice(OptionalInt.of(price));
    }

    public static IndexedItem forWinningBid(UUID itemId, int price, UUID winner) {
        return new IndexedItem(itemId).withPrice(OptionalInt.of(price)).withWinner(Optional.of(winner));
    }

    public static IndexedItem forAuctionStart(UUID itemId, Instant startDate, Instant endDate) {
        return new IndexedItem(itemId).withAuctionStart(Optional.of(startDate)).withAuctionEnd(Optional.of(endDate)).withStatus(Optional.of(ItemStatus.AUCTION));
    }

    public static IndexedItem forAuctionFinish(UUID itemId) {
        return new IndexedItem(itemId).withStatus(Optional.of(ItemStatus.COMPLETED));
    }

    public static IndexedItem forItemDetails(UUID itemId, UUID creatorId , String title, String description, ItemStatus itemStatus, String currencyId) {
        return new IndexedItem(itemId)
                .withCreatorId(Optional.of(creatorId))
                .withTitle(Optional.of(title))
                .withDescription(Optional.of(description))
                .withStatus(Optional.of(itemStatus))
                .withCurrencyId(Optional.of(currencyId));
    }
}
