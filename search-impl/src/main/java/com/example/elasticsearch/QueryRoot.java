package com.example.elasticsearch;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class QueryRoot {

    Query query;
    @JsonProperty("from")
    int pageNumber;
    @JsonProperty("size")
    int pageSize;

    @JsonCreator
    QueryRoot(Query query, int pageNumber, int pageSize) {
        this.query = query;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    QueryRoot(Query query) {
        this(query, 0, 15);
    }

    public QueryRoot withPagination(int pageNumber, int pageSize) {
        return new QueryRoot(this.query, pageNumber, pageSize);
    }
}
