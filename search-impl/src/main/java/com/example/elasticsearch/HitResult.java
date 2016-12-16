package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

/**
 *
 */
@Value
public class HitResult {
    IndexedItem item;

    @JsonCreator
    public HitResult(@JsonProperty("_source") IndexedItem item) {
        this.item = item;
    }

    public IndexedItem getItem() {
        return item;
    }
}
