package com.example.auction.item.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.util.Optional;
import java.util.UUID;

public interface PItemCommand extends Jsonable {

    enum GetItem implements PItemCommand, PersistentEntity.ReplyType<Optional<PItem>> {
        INSTANCE
    }

    @Value
    final class CreateItem implements PItemCommand, PersistentEntity.ReplyType<Done> {
        PItem item;

        @JsonCreator
        // TODO: change payload of this command.
        public CreateItem(PItem item) {
            this.item = item;
        }

    }


    @Value
    final class UpdateItem implements PItemCommand, PersistentEntity.ReplyType<PItem> {
         UUID commander;
         PItemData itemData;

        /**
         * @param commander the UUID of the user requesting this update. PersistentEntity must assert it's the same
         *                  than the Item creator.
         * @param itemData
         */
        @JsonCreator
        public UpdateItem(UUID commander, PItemData itemData) {
            this.commander = commander;
            this.itemData = itemData;
        }
    }

    @Value
    final class StartAuction implements PItemCommand, PersistentEntity.ReplyType<Done> {
         UUID userId;

        @JsonCreator
        public StartAuction(UUID userId) {
            this.userId = userId;
        }
    }

    @Value
    final class UpdatePrice implements PItemCommand, PersistentEntity.ReplyType<Done> {
         int price;
        
        @JsonCreator
        public UpdatePrice(int price) {
            this.price = price;
        }
    }

    @Value
    final class FinishAuction implements PItemCommand, PersistentEntity.ReplyType<Done> {
         Optional<UUID> winner;
         int price;

        @JsonCreator
        public FinishAuction(Optional<UUID> winner, int price) {
            this.winner = winner;
            this.price = price;
        }
    }
}
