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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PItem pItem = (PItem) o;

        if (increment != pItem.increment) return false;
        if (reservePrice != pItem.reservePrice) return false;
        if (price != pItem.price) return false;
        if (id != null ? !id.equals(pItem.id) : pItem.id != null) return false;
        if (creator != null ? !creator.equals(pItem.creator) : pItem.creator != null) return false;
        if (title != null ? !title.equals(pItem.title) : pItem.title != null) return false;
        if (description != null ? !description.equals(pItem.description) : pItem.description != null) return false;
        if (currencyId != null ? !currencyId.equals(pItem.currencyId) : pItem.currencyId != null) return false;
        if (status != pItem.status) return false;
        if (auctionDuration != null ? !auctionDuration.equals(pItem.auctionDuration) : pItem.auctionDuration != null)
            return false;
        if (auctionStart != null ? !auctionStart.equals(pItem.auctionStart) : pItem.auctionStart != null) return false;
        if (auctionEnd != null ? !auctionEnd.equals(pItem.auctionEnd) : pItem.auctionEnd != null) return false;
        return auctionWinner != null ? auctionWinner.equals(pItem.auctionWinner) : pItem.auctionWinner == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (creator != null ? creator.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (currencyId != null ? currencyId.hashCode() : 0);
        result = 31 * result + increment;
        result = 31 * result + reservePrice;
        result = 31 * result + price;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (auctionDuration != null ? auctionDuration.hashCode() : 0);
        result = 31 * result + (auctionStart != null ? auctionStart.hashCode() : 0);
        result = 31 * result + (auctionEnd != null ? auctionEnd.hashCode() : 0);
        result = 31 * result + (auctionWinner != null ? auctionWinner.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PItemFields{" +
                "id=" + id +
                ", creator=" + creator +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", currencyId='" + currencyId + '\'' +
                ", increment=" + increment +
                ", reservePrice=" + reservePrice +
                ", price=" + price +
                ", status=" + status +
                ", auctionDuration=" + auctionDuration +
                ", auctionStart=" + auctionStart +
                ", auctionEnd=" + auctionEnd +
                ", auctionWinner=" + auctionWinner +
                '}';
    }


    /**
     * Returns a copy of this instance with updates on the publicly editable fields..
     * @param description
     * @return
     */
    public PItem withFields(String title, String description, String currencyId, int increment, int reservePrice, Duration auctionDuration){
        return new PItem(
                this.getId(),
                this.getCreator(),
                title ,
                description,
                currencyId,
                increment ,
                reservePrice,
                this.getPrice(),
                this.getStatus(),
                auctionDuration,
                this.getAuctionStart(),
                this.getAuctionEnd(),
                this.getAuctionWinner()
        );
    }

    /**
     * Returns a copy of this instance with the new description.
     * @param description
     * @return
     */
    public PItem withDescription(String description) {
        return new PItem(
                this.getId(),
                this.getCreator(),
                this.getTitle() ,
                description,
                this.getCurrencyId(),
                this.getIncrement() ,
                this.getReservePrice() ,
                this.getPrice(),
                this.getStatus(),
                this.getAuctionDuration(),
                this.getAuctionStart(),
                this.getAuctionEnd(),
                this.getAuctionWinner()
        );
    }
}
