package com.example.elasticsearch;

import com.example.auction.item.api.Item;
import com.example.auction.item.api.ItemStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;


// when Optionals are empty, don't add the field on the JSON sent to ES. This is part of the partial update semantics.
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Value
public class IndexedItem {
    UUID itemId;
    Optional<UUID> creatorId;
    Optional<String> title;
    Optional<String> description;
    Optional<String> currencyId;
    OptionalInt increment;
    OptionalInt price;
    Optional<ItemStatus> status;
    Optional<Instant> auctionStart;
    Optional<Instant> auctionEnd;
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

    public static IndexedItem forPrice(UUID itemId, int price) {
        return new IndexedItem(
                itemId,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                OptionalInt.empty(),
                OptionalInt.of(price),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

    public static IndexedItem forWinningBid(UUID itemId, int price, UUID winner) {
        return new IndexedItem(
                itemId,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                OptionalInt.empty(),
                OptionalInt.of(price),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.of(winner)
        );
    }

    public static IndexedItem forAuctionStart(UUID itemId, Instant startDate, Instant endDate) {
        return new IndexedItem(
                itemId,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                OptionalInt.empty(),
                OptionalInt.empty(),
                Optional.of(ItemStatus.CREATED),
                Optional.of(startDate),
                Optional.of(endDate),
                Optional.empty()
        );
    }

    public static IndexedItem forAuctionFinish(UUID itemId, Item item) {
        return new IndexedItem(itemId,
                Optional.of(item.getCreator()),
                Optional.of(item.getItemData().getTitle()),
                Optional.of(item.getItemData().getDescription()),
                Optional.of(item.getItemData().getCurrencyId()),
                OptionalInt.of(item.getItemData().getIncrement()),
                OptionalInt.of(item.getPrice()),
                Optional.of(ItemStatus.COMPLETED),
                item.getAuctionStart(),
                item.getAuctionEnd(),
                item.getAuctionWinner());
    }

    public static IndexedItem forItemDetails(UUID itemId, UUID creatorId, String title, String description, ItemStatus itemStatus, String currencyId) {
        return new IndexedItem(
                itemId,
                Optional.of(creatorId),
                Optional.of(title),
                Optional.of(description),
                Optional.of(currencyId),
                OptionalInt.empty(),
                OptionalInt.empty(),
                Optional.of(itemStatus),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
    }

}
