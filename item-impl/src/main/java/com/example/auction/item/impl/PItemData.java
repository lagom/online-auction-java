package com.example.auction.item.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;
import lombok.experimental.Wither;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * This class uses Lombok's '@Value' annotation to become immutable. See https://projectlombok.org/features/Value.html
 */
@Value
public class PItemData implements Jsonable {

    String title;
    /**
     * Uses Lombok's @Wither since 'description' may be changed separate from all other fields.
     */
    @Wither
    String description;
    String currencyId;
    int increment;
    int reservePrice;
    Duration auctionDuration;
    Optional<UUID> categoryId;

    @JsonCreator
    public PItemData(String title, String description, String currencyId, int increment, int reservePrice, Duration auctionDuration, Optional<UUID> categoryId) {
        this.title = title;
        this.description = description;
        this.currencyId = currencyId;
        this.increment = increment;
        this.reservePrice = reservePrice;
        this.auctionDuration = auctionDuration;
        this.categoryId = categoryId;
    }

    /**
     * @return true iff this and that differ on Description only.
     */
    public boolean differOnDescriptionOnly(PItemData that) {
        return !this.equals(that) && // they differ AND
                this.equals(that.withDescription(description)); // they don't differ if using same description
    }

}
