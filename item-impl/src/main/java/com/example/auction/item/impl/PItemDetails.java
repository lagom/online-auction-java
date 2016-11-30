package com.example.auction.item.impl;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.Duration;

/**
 *
 */
public class PItemDetails {

    private final String title;
    private final String description;
    private final String currencyId;
    private final int increment;
    private final int reservePrice;
    private final Duration auctionDuration;

    @JsonCreator
    public PItemDetails(String title, String description, String currencyId, int increment, int reservePrice, Duration auctionDuration) {
        this.title = title;
        this.description = description;
        this.currencyId = currencyId;
        this.increment = increment;
        this.reservePrice = reservePrice;
        this.auctionDuration = auctionDuration;
    }

    public PItemDetails withDescription(String description) {
        return new PItemDetails(
                getTitle(),
                description,
                getCurrencyId(),
                getIncrement(),
                getReservePrice(),
                getAuctionDuration());
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

    public Duration getAuctionDuration() {
        return auctionDuration;
    }

    /**
     * @return true iff this and that differ on Description only.
     */
    public boolean differOnDescriptionOnly(PItemDetails that) {
        return !this.equals(that) && // they differ AND
                this.equals(that.withDescription(this.getDescription())); // they don't differ if using same description
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PItemDetails that = (PItemDetails) o;

        if (increment != that.increment) return false;
        if (reservePrice != that.reservePrice) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (currencyId != null ? !currencyId.equals(that.currencyId) : that.currencyId != null) return false;
        return auctionDuration != null ? auctionDuration.equals(that.auctionDuration) : that.auctionDuration == null;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (currencyId != null ? currencyId.hashCode() : 0);
        result = 31 * result + increment;
        result = 31 * result + reservePrice;
        result = 31 * result + (auctionDuration != null ? auctionDuration.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PItemDetails{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", currencyId='" + currencyId + '\'' +
                ", increment=" + increment +
                ", reservePrice=" + reservePrice +
                ", auctionDuration=" + auctionDuration +
                '}';
    }

}
