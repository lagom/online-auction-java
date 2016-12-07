package com.example.elasticsearch;


import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

@Value
public class QueryRoot {

    Query query;

    @JsonCreator
    public QueryRoot(Query query) {
        this.query = query;
    }

}
