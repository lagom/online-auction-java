package com.example.auction.item.api;

import java.util.UUID;

/**
 * The most important fields of an item.
 */
public final class ItemSummary {

    private final UUID id;
    private final String title;
    private final String currencyId;
    private final int reservePrice;
    private final ItemStatus status;

    public ItemSummary(UUID id, String title, String currencyId, int reservePrice, ItemStatus status) {
        this.id = id;
        this.title = title;
        this.currencyId = currencyId;
        this.reservePrice = reservePrice;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public int getReservePrice() {
        return reservePrice;
    }

    public ItemStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemSummary that = (ItemSummary) o;

        if (reservePrice != that.reservePrice) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (currencyId != null ? !currencyId.equals(that.currencyId) : that.currencyId != null) return false;
        return status == that.status;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (currencyId != null ? currencyId.hashCode() : 0);
        result = 31 * result + reservePrice;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ItemSummary{" +
            "id=" + id +
            ", title='" + title + '\'' +
            ", currencyId='" + currencyId + '\'' +
            ", reservePrice=" + reservePrice +
            ", status=" + status +
            '}';
    }
}
