package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 *
 */
public class Query {

    BooleanQuery bool;

    @JsonCreator
    public Query(BooleanQuery bool) {
        this.bool = bool;
    }

    public boolean test(IndexedItem item){
        return bool.test(item) ;
    }

}
