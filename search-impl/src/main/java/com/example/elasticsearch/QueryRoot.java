package com.example.elasticsearch;


import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

@Value
public class QueryRoot {

    BooleanQuery bool;

    @JsonCreator
    public QueryRoot(BooleanQuery bool) {
        this.bool = bool;
    }

    public boolean test(IndexedItem item){
        return bool.test(item) ;
    }
}
