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

        @Override
        public Predicate<? super IndexedItem> predicate() {
            return (ii) ->
                    ii.getPrice().isPresent()
                            &&
                            ii.getPrice().getAsInt() <= price.getLte().orElse(Integer.MAX_VALUE) &&
                            ii.getPrice().getAsInt() >= price.getGte().orElse(Integer.MIN_VALUE);
        }
    }
}
