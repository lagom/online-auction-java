package com.example.auction.transaction.impl;

import com.example.auction.transaction.api.DeliveryInfo;
import com.example.auction.transaction.api.TransactionInfo;

import java.util.Optional;

public class TransactionMappers {

    public static Optional<DeliveryInfo> toApi(Optional<DeliveryData> data) {
        if(data.isPresent()) {
            return Optional.of(new DeliveryInfo(
                    data.get().getAddressLine1(),
                    data.get().getAddressLine2(),
                    data.get().getCity(),
                    data.get().getState(),
                    data.get().getPostalCode(),
                    data.get().getCountry()
            ));
        }
        else
            return Optional.empty();
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

    public static TransactionInfo toApi(TransactionState data) {
        Transaction transaction = data.getTransaction().get();
        return new TransactionInfo(
                transaction.getItemId(),
                transaction.getCreator(),
                transaction.getWinner(),
                transaction.getItemData(),
                transaction.getItemPrice(),
                transaction.getDeliveryPrice(),
                toApi(transaction.getDeliveryData()),
                data.getStatus().transactionStatus
        );
    }
}
