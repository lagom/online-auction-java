package com.example.auction.transaction.impl;

import com.example.auction.transaction.api.DeliveryInfo;
import com.example.auction.transaction.api.TransactionInfo;

import java.util.Optional;

public class TransactionMappers {

    public static Optional<DeliveryInfo> toApi(Optional<DeliveryData> data) {
        return data.map(deliveryData -> new DeliveryInfo(
                data.get().getAddressLine1(),
                data.get().getAddressLine2(),
                data.get().getCity(),
                data.get().getState(),
                data.get().getPostalCode(),
                data.get().getCountry())
        );
    }

    public static DeliveryData fromApi(DeliveryInfo data) {
        return new DeliveryData(
                data.getAddressLine1(),
                data.getAddressLine2(),
                data.getCity(),
                data.getState(),
                data.getPostalCode(),
                data.getCountry()
        );
    }

    public static Optional<TransactionInfo> toApi(TransactionState data) {
        return data.getTransaction().map(transaction ->
            new TransactionInfo(
                    transaction.getItemId(),
                    transaction.getCreator(),
                    transaction.getWinner(),
                    transaction.getItemData(),
                    transaction.getItemPrice(),
                    transaction.getDeliveryPrice(),
                    toApi(transaction.getDeliveryData()),
                    data.getStatus().transactionStatus
            )
        );
    }
}