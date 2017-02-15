package com.example.elasticsearch;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.Optional;

@Value
public class QueryRoot {

    Query query;
    @JsonProperty("from")
    int pageNumber;
    @JsonProperty("size")
    int pageSize;

    @JsonProperty("sort")
    SortField[] sort = new SortField[]{
//            SortField.status, // keyword ES datatype.
            SortField.auctionEndDescending,
            SortField.priceAscending
    };

    @JsonCreator
    QueryRoot(Query query, int pageNumber, int pageSize) {
        this.query = query;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }


}
