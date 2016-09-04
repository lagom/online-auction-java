package com.example.auction.item.api;

import java.util.Optional;
import java.util.UUID;

public abstract class DeliveryOption {

    public final class PickUp extends DeliveryOption {
    }

    public final class Deliver extends DeliveryOption {
        private final String name;
        private final int price;
        private final Location location;
    }

    public final class ByNegotiation extends DeliveryOption {
        private final Location location;
    }

}
