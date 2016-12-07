package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.OptionalInt;
import java.util.function.Predicate;

/**
 *
 */
public interface RangeFilterField extends Filter {

    @Value
    class PriceRange implements RangeFilterField {
        RangeInt price;

        @JsonCreator
        public PriceRange(int maxPrice) {
            price = new RangeInt(OptionalInt.of(maxPrice), OptionalInt.empty());
        }

    }
}
