package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 *
 */
@Value
public class Query {

    BooleanQuery bool;

    @JsonCreator
    public Query(@JsonProperty("bool") BooleanQuery bool) {
        this.bool = bool;
    }

}
