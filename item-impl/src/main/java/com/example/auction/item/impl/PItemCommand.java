package com.example.auction.item.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface PItemCommand extends Jsonable {

    enum GetItem implements PItemCommand, PersistentEntity.ReplyType<Optional<PItem>> {
        INSTANCE
    }

    final class CreateItem implements PItemCommand, PersistentEntity.ReplyType<Done> {
        private final PItem item;

        @JsonCreator
        public CreateItem(PItem item) {
            this.item = item;
        }

        public PItem getItem() {
            return item;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CreateItem that = (CreateItem) o;
            return item.equals(that.item);
        }

        @Override
        public int hashCode() {
            return item.hashCode();
        }
    }


    final class UpdateItem implements PItemCommand, PersistentEntity.ReplyType<PUpdateItemResult> {
        private final UUID id;
        private final UUID creator;
        final String title;
        private final String description;
        private final String currencyId;
        private final int increment;
        private final int reservePrice;
        private final Duration auctionDuration;

        @JsonCreator
        public UpdateItem(UUID id, UUID creator, String title, String description, String currencyId, int increment, int reservePrice, Duration auctionDuration) {
            this.id = id;
            this.creator = creator;
            this.title = title;
            this.description = description;
            this.currencyId = currencyId;
            this.increment = increment;
            this.reservePrice = reservePrice;
            this.auctionDuration = auctionDuration;
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

        public Duration getAuctionDuration() {
            return auctionDuration;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UpdateItem that = (UpdateItem) o;

            if (increment != that.increment) return false;
            if (reservePrice != that.reservePrice) return false;
            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            if (creator != null ? !creator.equals(that.creator) : that.creator != null) return false;
            if (title != null ? !title.equals(that.title) : that.title != null) return false;
            if (description != null ? !description.equals(that.description) : that.description != null) return false;
            if (currencyId != null ? !currencyId.equals(that.currencyId) : that.currencyId != null) return false;
            return auctionDuration != null ? auctionDuration.equals(that.auctionDuration) : that.auctionDuration == null;
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
            result = 31 * result + (auctionDuration != null ? auctionDuration.hashCode() : 0);
            return result;
        }
    }

    final class StartAuction implements PItemCommand, PersistentEntity.ReplyType<Done> {
        private final UUID userId;

        @JsonCreator
        public StartAuction(UUID userId) {
            this.userId = userId;
        }

        public UUID getUserId() {
            return userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StartAuction that = (StartAuction) o;
            return userId.equals(that.userId);

        }

        @Override
        public int hashCode() {
            return userId.hashCode();
        }
    }

    final class UpdatePrice implements PItemCommand, PersistentEntity.ReplyType<Done> {
        private final int price;

        @JsonCreator
        public UpdatePrice(int price) {
            this.price = price;
        }

        public int getPrice() {
            return price;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UpdatePrice that = (UpdatePrice) o;
            return price == that.price;

        }

        @Override
        public int hashCode() {
            return price;
        }
    }

    final class FinishAuction implements PItemCommand, PersistentEntity.ReplyType<Done> {
        private final Optional<UUID> winner;
        private final int price;

        @JsonCreator
        public FinishAuction(Optional<UUID> winner, int price) {
            this.winner = winner;
            this.price = price;
        }

        public Optional<UUID> getWinner() {
            return winner;
        }

        public int getPrice() {
            return price;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FinishAuction that = (FinishAuction) o;

            if (price != that.price) return false;
            return winner.equals(that.winner);

        }

        @Override
        public int hashCode() {
            int result = winner.hashCode();
            result = 31 * result + price;
            return result;
        }
    }
}
