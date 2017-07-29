package com.example.auction.transaction.impl;

import com.example.auction.item.api.Location;
import lombok.Value;

public abstract class Payment {

    @Value
    public final class CashOnDelivery extends Payment {
    }

    @Value
    public final class CreditCard extends Payment {

        private final String cardNumber;
        private final String cardType;
        private final String cardName;
        private final String cardCVC;
        private final String expirationDate;

        private final String firstName;
        private final String lastName;
        private final String billingAddress;
        private final Location location;
    }
}
