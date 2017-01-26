package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

/**
 *
 */
@Value
public class UpdateIndexItem {
    // Use partial update semantics on ES
    IndexedItem doc;
    // Use partial update semantics on ES
    boolean doc_as_upsert = true;

    @JsonCreator
    public UpdateIndexItem(IndexedItem doc) {
        this.doc = doc;
    }
}
