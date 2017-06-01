package com.example.auction.transaction.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

@Value
public class DeliveryData {
    private final String addressLine1;
    private final String addressLine2;
    private final String city;
    private final String state;
    private final int postalCode;
    private final String country;

    @JsonCreator
    public DeliveryData(String addressLine1, String addressLine2, String city, String state, int postalCode, String country) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }
}
