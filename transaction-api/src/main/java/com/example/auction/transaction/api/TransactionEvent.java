package com.example.auction.transaction.api;

import java.util.UUID;

public interface TransactionEvent {

    final class DeliveryByNegotiation implements TransactionEvent {

        private final UUID itemId;

        public DeliveryByNegotiation(UUID itemId) {
            this.itemId = itemId;
        }
    }

    final class DeliveryPriceUpdated implements TransactionEvent {

        private final UUID itemId;

        public DeliveryPriceUpdated(UUID itemId) {
            this.itemId = itemId;
        }
    }

    final class PaymentConfirmed implements TransactionEvent {

        private final UUID itemId;

        public PaymentConfirmed(UUID itemId) {
            this.itemId = itemId;
        }
    }

    final class PaymentFailed implements TransactionEvent {

        private final UUID itemId;

        public PaymentFailed(UUID itemId) {
            this.itemId = itemId;
        }
    }

    final class ItemDispatched implements TransactionEvent {

        private final UUID itemId;

        public ItemDispatched(UUID itemId) {
            this.itemId = itemId;
        }
    }

    final class ItemReceived implements TransactionEvent {

        private final UUID itemId;

        public ItemReceived(UUID itemId) {
            this.itemId = itemId;
        }
    }

    final class MessageSent implements TransactionEvent {

        private final UUID itemId;
        private final TransactionMessage message;

        public MessageSent(UUID itemId, TransactionMessage message) {
            this.itemId = itemId;
            this.message = message;
        }
    }

    final class RefundConfirmed implements TransactionEvent {

        private final UUID itemId;

        public RefundConfirmed(UUID itemId) {
            this.itemId = itemId;
        }
    }
}
