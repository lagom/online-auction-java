package com.example.auction.transaction.impl;

import com.example.auction.transaction.api.DeliveryInfo;
import com.example.auction.transaction.api.PaymentInfo;
import com.example.auction.transaction.api.PaymentInfoStatus;
import com.example.auction.transaction.api.TransactionInfo;

import java.util.Optional;

public class TransactionMappers {

    public static Optional<DeliveryInfo> toApiDelivery(Optional<DeliveryData> data) {
        return data.map(deliveryData -> new DeliveryInfo(
                data.get().getAddressLine1(),
                data.get().getAddressLine2(),
                data.get().getCity(),
                data.get().getState(),
                data.get().getPostalCode(),
                data.get().getCountry())
        );
    }

    public static DeliveryData fromApiDelivery(DeliveryInfo data) {
        return new DeliveryData(
                data.getAddressLine1(),
                data.getAddressLine2(),
                data.getCity(),
                data.getState(),
                data.getPostalCode(),
                data.getCountry()
        );
    }

    public static Optional<PaymentInfo> toApiPayment(Optional<Payment> data) {
        return data.flatMap(payment -> {
                    if (payment instanceof Payment.Offline)
                        return Optional.of(new PaymentInfo.Offline(((Payment.Offline) payment).getComment()));
                    else
                        return Optional.empty();
                }
        );
    }

    public static Payment fromApiPayment(PaymentInfo data) {
        if (data instanceof PaymentInfo.Offline)
            return new Payment.Offline(((PaymentInfo.Offline) data).getComment());
        else
            throw new IllegalArgumentException("Mapping non payment class");
    }

    public static TransactionInfo toApi(TransactionState data) {
        // TransactionEntity verifies if a transaction in TransactionState is set
        // This code is called after this verification was done from TransactionServiceImpl
        // We can get() safely
        Transaction transaction = data.getTransaction().get();
        return new TransactionInfo(
                transaction.getItemId(),
                transaction.getCreator(),
                transaction.getWinner(),
                transaction.getItemData(),
                transaction.getItemPrice(),
                toApiDelivery(transaction.getDeliveryData()),
                transaction.getDeliveryPrice(),
                toApiPayment(transaction.getPayment()),
                data.getStatus().transactionInfoStatus
        );
    }

    public static PaymentStatus fromApi(PaymentInfoStatus paymentInfoStatus) {
        switch(paymentInfoStatus) {
            case APPROVED:
                return PaymentStatus.APPROVED;
            case REJECTED:
                return PaymentStatus.REJECTED;
            default:
                throw new IllegalStateException("Mapping non payment status class");
        }
    }
}