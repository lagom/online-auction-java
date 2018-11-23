package com.example.auction.item.api;

import lombok.EqualsAndHashCode;
import lombok.Value;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryOption {

    final class PickUp implements DeliveryOption {}

    @Value
    final class Deliver implements DeliveryOption {
        private final String name;
        private final int price;
        private final Location location;

        public Deliver(String name, int price, Location location) {
            this.name = name;
            this.price = price;
            this.location = location;
        }
    }

    @Value
    @EqualsAndHashCode(callSuper=false)
    final class ByNegotiation implements DeliveryOption {
        private final Location location;

        public ByNegotiation(Location location) {
            this.location = location;
        }
    }

}
