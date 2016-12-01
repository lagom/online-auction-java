package com.example.auction.item.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Data;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Data
public class PItem implements Jsonable {

    private final UUID id;
    private final UUID creator;
    private final PItemData itemData;
    private final int price;
    private final PItemStatus status;
    private final Optional<Instant> auctionStart;
    private final Optional<Instant> auctionEnd;
    private final Optional<UUID> auctionWinner;

    @JsonCreator
    private PItem(UUID id, UUID creator, PItemData pItemData, int price, PItemStatus status,
                  Optional<Instant> auctionStart, Optional<Instant> auctionEnd, Optional<UUID> auctionWinner) {
        this.id = id;
        this.creator = creator;
        this.itemData = pItemData;
        this.price = price;
        this.status = status;
        this.auctionStart = auctionStart;
        this.auctionEnd = auctionEnd;
        this.auctionWinner = auctionWinner;
    }

    public PItem(UUID id, UUID creator, PItemData itemData) {
        this.id = id;
        this.creator = creator;
        this.itemData = itemData;
        this.price = 0;
        this.status = PItemStatus.CREATED;
        this.auctionStart = Optional.empty();
        this.auctionEnd = Optional.empty();
        this.auctionWinner = Optional.empty();
    }

    public PItem start(Instant startTime) {
        assert status == PItemStatus.CREATED;
        return new PItem(id, creator, itemData, price, PItemStatus.AUCTION,
                Optional.of(startTime), Optional.of(startTime.plus(itemData.getAuctionDuration())), auctionWinner);
    }

    public PItem end(Optional<UUID> winner, int price) {
        assert status == PItemStatus.AUCTION;
        return new PItem(id, creator, itemData, price, PItemStatus.COMPLETED, auctionStart, auctionEnd, winner);
    }

    public PItem updatePrice(int price) {
        assert status == PItemStatus.AUCTION;
        return new PItem(id, creator, itemData, price, status, auctionStart, auctionEnd, auctionWinner);
    }

    public PItem cancel() {
        assert status == PItemStatus.AUCTION || status == PItemStatus.CREATED;
        return new PItem(id, creator, itemData, price, PItemStatus.CANCELLED, auctionStart, auctionEnd, auctionWinner);
    }
    /**
     * Returns a copy of this instance with updates on the publicly editable fields.
     */
    public PItem withDetails(PItemData details) {
        return new PItem(id, creator, details, price, status, auctionStart, auctionEnd, auctionWinner);
    }

    /**
     * Returns a copy of this instance with the new description.
     */
    public PItem withDescription(String description) {
        return new PItem(id, creator, itemData.withDescription(description), price, status, auctionStart, auctionEnd, auctionWinner);
    }


}
