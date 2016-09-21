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
}
