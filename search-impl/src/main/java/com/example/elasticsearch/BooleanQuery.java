package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import org.pcollections.PSequence;

import java.util.Arrays;


@Value
class BooleanQuery {

    @JsonProperty("must_not")
    Filter mustNot;
    PSequence<Filter> must;

    @JsonCreator
    public BooleanQuery(Filter mustNot, PSequence<Filter> must) {
        this.mustNot = mustNot;
        this.must = must;
    }
}
