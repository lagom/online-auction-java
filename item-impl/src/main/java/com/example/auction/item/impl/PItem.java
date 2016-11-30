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
    private final PItemDetails itemDetails;
    private final int price;
    private final PItemStatus status;
    private final Optional<Instant> auctionStart;
    private final Optional<Instant> auctionEnd;
    private final Optional<UUID> auctionWinner;

    @JsonCreator
    private PItem(UUID id, UUID creator, PItemDetails pItemDetails, int price, PItemStatus status,
                  Optional<Instant> auctionStart, Optional<Instant> auctionEnd, Optional<UUID> auctionWinner) {
        this.id = id;
        this.creator = creator;
        this.itemDetails = pItemDetails;
        this.price = price;
        this.status = status;
        this.auctionStart = auctionStart;
        this.auctionEnd = auctionEnd;
        this.auctionWinner = auctionWinner;
    }

    @Deprecated
    private PItem(UUID id, UUID creator, String title, String description, String currencyId,
                  int increment, int reservePrice, int price, PItemStatus status, Duration auctionDuration,
                  Optional<Instant> auctionStart, Optional<Instant> auctionEnd, Optional<UUID> auctionWinner) {
        this.id = id;
        this.creator = creator;
        this.itemDetails = new PItemDetails(title, description, currencyId, increment, reservePrice, auctionDuration);
        this.price = price;
        this.status = status;
        this.auctionStart = auctionStart;
        this.auctionEnd = auctionEnd;
        this.auctionWinner = auctionWinner;
    }

    public PItem(UUID id, UUID creator, String title, String description, String currencyId, int increment, int reservePrice, Duration auctionDuration) {
        this.id = id;
        this.creator = creator;
        this.itemDetails = new PItemDetails(title, description, currencyId, increment, reservePrice, auctionDuration);

        this.price = 0;
        this.status = PItemStatus.CREATED;
        this.auctionStart = Optional.empty();
        this.auctionEnd = Optional.empty();
        this.auctionWinner = Optional.empty();
    }

    public PItem start(Instant startTime) {
        assert status == PItemStatus.CREATED;
        return new PItem(id, creator, itemDetails, price, PItemStatus.AUCTION,
                Optional.of(startTime), Optional.of(startTime.plus(itemDetails.getAuctionDuration())), auctionWinner);
    }

    public PItem end(Optional<UUID> winner, int price) {
        assert status == PItemStatus.AUCTION;
        return new PItem(id, creator, itemDetails, price, PItemStatus.COMPLETED, auctionStart, auctionEnd, winner);
    }

    public PItem updatePrice(int price) {
        assert status == PItemStatus.AUCTION;
        return new PItem(id, creator, itemDetails, price, status, auctionStart, auctionEnd, auctionWinner);
    }

    public PItem cancel() {
        assert status == PItemStatus.AUCTION || status == PItemStatus.CREATED;
        return new PItem(id, creator, itemDetails, price, PItemStatus.CANCELLED, auctionStart, auctionEnd, auctionWinner);
    }
    /**
     * Returns a copy of this instance with updates on the publicly editable fields.
     */
    public PItem withDetails(PItemDetails details) {
        return new PItem(id, creator, details, price, status, auctionStart, auctionEnd, auctionWinner);
    }

    /**
     * Returns a copy of this instance with the new description.
     */
    public PItem withDescription(String description) {
        return new PItem(id, creator, itemDetails.withDescription(description), price, status, auctionStart, auctionEnd, auctionWinner);
    }


    public UUID getId() {
        return id;
    }

    public UUID getCreator() {
        return creator;
    }

    public String getTitle() {
        return itemDetails.getTitle();
    }

    public String getDescription() {
        return itemDetails.getDescription();
    }

    public String getCurrencyId() {
        return itemDetails.getCurrencyId();
    }

    public int getIncrement() {
        return itemDetails.getIncrement();
    }

    public int getReservePrice() {
        return itemDetails.getReservePrice();
    }

    public Duration getAuctionDuration() {
        return itemDetails.getAuctionDuration();
    }

    public int getPrice() {
        return price;
    }

    public PItemStatus getStatus() {
        return status;
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

    public PItemDetails getItemDetails() {
        return itemDetails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PItem pItem = (PItem) o;

        if (price != pItem.price) return false;
        if (id != null ? !id.equals(pItem.id) : pItem.id != null) return false;
        if (creator != null ? !creator.equals(pItem.creator) : pItem.creator != null) return false;
        if (itemDetails != null ? !itemDetails.equals(pItem.itemDetails) : pItem.itemDetails != null) return false;
        if (status != pItem.status) return false;
        if (auctionStart != null ? !auctionStart.equals(pItem.auctionStart) : pItem.auctionStart != null) return false;
        if (auctionEnd != null ? !auctionEnd.equals(pItem.auctionEnd) : pItem.auctionEnd != null) return false;
        return auctionWinner != null ? auctionWinner.equals(pItem.auctionWinner) : pItem.auctionWinner == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (creator != null ? creator.hashCode() : 0);
        result = 31 * result + (itemDetails != null ? itemDetails.hashCode() : 0);
        result = 31 * result + price;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (auctionStart != null ? auctionStart.hashCode() : 0);
        result = 31 * result + (auctionEnd != null ? auctionEnd.hashCode() : 0);
        result = 31 * result + (auctionWinner != null ? auctionWinner.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return "PItem{" +
                "id=" + id +
                ", creator=" + creator +
                ", itemDetails=" + itemDetails +
                ", price=" + price +
                ", status=" + status +
                ", auctionStart=" + auctionStart +
                ", auctionEnd=" + auctionEnd +
                ", auctionWinner=" + auctionWinner +
                '}';
    }

}
