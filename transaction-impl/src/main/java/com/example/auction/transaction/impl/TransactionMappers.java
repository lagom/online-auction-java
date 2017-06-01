package com.example.auction.transaction.impl;

import com.example.auction.transaction.api.DeliveryInfo;

public class TransactionMappers {

    public static DeliveryInfo toApi(DeliveryData data) {
        return new DeliveryInfo(
                data.getAddressLine1(),
                data.getAddressLine2(),
                data.getCity(),
                data.getState(),
                data.getPostalCode(),
                data.getCountry()
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
}
