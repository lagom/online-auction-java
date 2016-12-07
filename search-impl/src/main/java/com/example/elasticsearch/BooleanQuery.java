package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.Arrays;


@Value
class BooleanQuery {

    @JsonProperty("must_not")
    Filter mustNot;
    Filter[] should;

    @JsonCreator
    public BooleanQuery(Filter mustNot, Filter... should) {
        this.mustNot = mustNot;
        this.should = should;
    }

}
