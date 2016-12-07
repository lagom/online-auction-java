package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.Arrays;

/**
 *
 */
@Value
class BooleanQuery {

    Filter[] should;

    @JsonCreator
    public BooleanQuery(Filter... should) {
        this.should = should;
    }

    public boolean test(IndexedItem item) {
        return Arrays.asList(should).stream().allMatch(f -> f.predicate().test(item));
    }
}
