package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.function.Predicate;


@Value
class RangeFilter implements  Filter {
    RangeFilterField range;

    @JsonCreator
    public RangeFilter(RangeFilterField range) {
        this.range = range;
    }
}

