package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

/**
 *
 */
@Value
 class MustFilter {
    RangeFilter range;

    @JsonCreator
    public MustFilter(RangeFilter range) {
        this.range = range;
    }

    public boolean test(IndexedItem item) {
        return range.predicate().test(item);

    }
}
