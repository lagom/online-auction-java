package com.example.auction.item.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;

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
        private final UUID commander;
        private final PItemDetails itemDetails;

        /**
         * @param commander the UUID of the user requesting this update. PersistenEntity must assert it's the same
         *                  than the PItem creator.
         * @param itemDetails
         */
        @JsonCreator
        public UpdateItem(UUID commander, PItemDetails itemDetails) {
            this.commander = commander;
            this.itemDetails = itemDetails;
        }

        public UUID getCommander() {
            return commander;
        }

        public PItemDetails getItemDetails() {
            return itemDetails;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UpdateItem that = (UpdateItem) o;

            if (commander != null ? !commander.equals(that.commander) : that.commander != null) return false;
            return itemDetails != null ? itemDetails.equals(that.itemDetails) : that.itemDetails == null;
        }

        @Override
        public int hashCode() {
            int result = commander != null ? commander.hashCode() : 0;
            result = 31 * result + (itemDetails != null ? itemDetails.hashCode() : 0);
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
