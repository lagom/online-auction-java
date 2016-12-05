package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 *
 */
@Value
public class HitResult {
    @JsonProperty("_source")
    IndexedItem item;
    @JsonProperty("_type")
    String type;

    @JsonCreator
    public HitResult(IndexedItem source, String type) {
        item = source;
        this.type = type;
    }

    public IndexedItem getItem() {
        return item;
    }
}
