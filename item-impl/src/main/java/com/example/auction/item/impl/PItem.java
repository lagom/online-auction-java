package com.example.auction.item.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class PItem implements Jsonable {

    private final UUID id;
    private final UUID creator;
    private final String title;
    private final String description;
    private final String currencyId;
    private final int increment;
    private final int reservePrice;
    private final int price;
    private final PItemStatus status;
    private final Duration auctionDuration;
    private final Optional<Instant> auctionStart;
    private final Optional<Instant> auctionEnd;
    private final Optional<UUID> auctionWinner;

    @JsonCreator
    private PItem(UUID id, UUID creator, String title, String description, String currencyId,
            int increment, int reservePrice, int price, PItemStatus status, Duration auctionDuration,
            Optional<Instant> auctionStart, Optional<Instant> auctionEnd, Optional<UUID> auctionWinner) {
        this.id = id;
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.currencyId = currencyId;
        this.increment = increment;
        this.reservePrice = reservePrice;
        this.price = price;
        this.status = status;
        this.auctionDuration = auctionDuration;
        this.auctionStart = auctionStart;
        this.auctionEnd = auctionEnd;
        this.auctionWinner = auctionWinner;
    }

    public PItem(UUID id, UUID creator, String title, String description, String currencyId, int increment, int reservePrice, Duration auctionDuration) {
        this.id = id;
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.currencyId = currencyId;
        this.increment = increment;
        this.reservePrice = reservePrice;
        this.auctionDuration = auctionDuration;

        this.price = 0;
        this.status = PItemStatus.CREATED;
        this.auctionStart = Optional.empty();
        this.auctionEnd = Optional.empty();
        this.auctionWinner = Optional.empty();
    }

    public PItem start(Instant startTime) {
        assert status == PItemStatus.CREATED;
        return new PItem(id, creator, title, description, currencyId, increment, reservePrice, price, PItemStatus.AUCTION, auctionDuration,
                Optional.of(startTime), Optional.of(startTime.plus(auctionDuration)), auctionWinner);
    }

    public PItem end(Optional<UUID> winner, int price) {
        assert status == PItemStatus.AUCTION;
        return new PItem(id, creator, title, description, currencyId, increment, reservePrice, price, PItemStatus.COMPLETED, auctionDuration,
                auctionStart, auctionEnd, winner);
    }

    public PItem updatePrice(int price) {
        assert status == PItemStatus.AUCTION;
        return new PItem(id, creator, title, description, currencyId, increment, reservePrice, price, status, auctionDuration,
                auctionStart, auctionEnd, auctionWinner);
    }

    public PItem cancel() {
        assert status == PItemStatus.AUCTION || status == PItemStatus.CREATED;
        return new PItem(id, creator, title, description, currencyId, increment, reservePrice, price, PItemStatus.CANCELLED, auctionDuration,
                auctionStart, auctionEnd, auctionWinner);
    }

    public UUID getId() {
        return id;
    }

    public UUID getCreator() {
        return creator;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public int getIncrement() {
        return increment;
    }

    public int getReservePrice() {
        return reservePrice;
    }

    public int getPrice() {
        return price;
    }

    public PItemStatus getStatus() {
        return status;
    }

    public Duration getAuctionDuration() {
        return auctionDuration;
    }

    public Optional<Instant> getAuctionStart() {
        return auctionStart;
    }

    public Optional<Instant> getAuctionEnd() {
        return auctionEnd;
    }

    public Optional<UUID> getAuctionWinner() {
        return auctionWinner;
    }

}
