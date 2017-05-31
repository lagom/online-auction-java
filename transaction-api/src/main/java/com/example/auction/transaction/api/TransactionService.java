package com.example.auction.transaction.api;

import static com.lightbend.lagom.javadsl.api.Service.named;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.broker.Topic;

/**
 * The transaction services.
 *
 * Handles the transaction of negotiating delivery info and making payment of an item that has completed an auction.
 *
 * A transaction is created when an AuctionFinished event is received
 */
public interface TransactionService extends Service {

    String TOPIC_ID = "transaction-TransactionEvent";

    //ServiceCall<TransactionMessage, Done> sendMessage(UUID itemId);

    //ServiceCall<DeliveryInfo, Done> submitDeliveryDetails(UUID itemId);

    //ServiceCall<Integer, Done> setDeliveryPrice(UUID itemId);

    //ServiceCall<PaymentInfo, Done> submitPaymentDetails(UUID itemId);

    //ServiceCall<NotUsed, Done> dispatchItem(UUID itemId);

    //ServiceCall<NotUsed, Done> receiveItem(UUID itemId);

    //ServiceCall<NotUsed, Done> initiateRefund(UUID itemId);

    /**
     * The transaction events topic.
     */
    //Topic<TransactionEvent> transactionEvents();

    @Override
    default Descriptor descriptor() {
        return named("transaction").withCalls(
                // No pathcalls for now ...
        );
    }

}
