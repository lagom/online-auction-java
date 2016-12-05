package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.Arrays;

/**
 *
 */
@Value
class BooleanQuery {

    Filter[] must;

    @JsonCreator
    public BooleanQuery(Filter... must) {
        this.must = must;
    }

    public boolean test(IndexedItem item) {
        return Arrays.asList(must).stream().allMatch(f -> f.predicate().test(item));
    }
}
