package com.example.auction.item.api;

import com.example.auction.item.api.DeliveryOption;
import com.example.auction.item.api.ItemStatus;
import com.example.auction.item.api.Location;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;
import org.pcollections.PSet;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * An item entity.
 */
@Value
@Builder
@Wither
public class Item {

    UUID id;
    UUID creator;
    ItemData itemData;
    int price;
    ItemStatus status;
    Optional<Instant> auctionStart;
    Optional<Instant> auctionEnd;
    Optional<UUID> auctionWinner;

    // null fields are temporarily null because they aren't implemented yet
    private final UUID categoryId = null;
    private final Location location = null;
    private final PSet<DeliveryOption> deliveryOptions = null;

    // TODO: review which constructors do we want for this class. Also consider lombok is providing constructors too...
    /**
     * Constructor that Jackson uses when deserialising items.
     */
    @JsonCreator
    private Item(UUID id, UUID creator,
                 ItemData itemData,
                 Optional<Integer> price,
                 Optional<ItemStatus> status,
                 Optional<Instant> auctionStart,
                 Optional<Instant> auctionEnd,
                 Optional<UUID> auctionWinner) {
        this.id = id;
        this.creator = creator;
        this.itemData = itemData;
        this.price = price.orElse(0);
        this.status = status.orElse(null);
        this.auctionStart = auctionStart;
        this.auctionEnd = auctionEnd;
        this.auctionWinner = auctionWinner;
    }

    public Item(UUID id, UUID creator,
                ItemData itemData,
                int price,
                ItemStatus status,
                Optional<Instant> auctionStart,
                Optional<Instant> auctionEnd,
                Optional<UUID> auctionWinner) {
        this.id = id;
        this.creator = creator;
        this.itemData = itemData;
        this.price = price;
        this.status = status;
        this.auctionStart = auctionStart;
        this.auctionEnd = auctionEnd;
        this.auctionWinner = auctionWinner;
    }
}
