package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.Arrays;

/**
 *
 */
@Value
class BooleanQuery {

    MustFilter[] must;

    @JsonCreator
    public BooleanQuery(MustFilter... must) {
        this.must = must;
    }

    public boolean test(IndexedItem item) {
        return Arrays.asList(must).stream().allMatch(m -> m.test(item));
    }
}
