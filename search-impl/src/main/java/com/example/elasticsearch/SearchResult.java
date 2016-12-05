package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.stream.Stream;

/**
 *
 */
@Value
public class SearchResult {
    Hits hits;

    @JsonCreator
    public SearchResult(Hits hits) {
        this.hits = hits;
    }

    public Stream<IndexedItem> getIndexedItem() {
        return hits.getHits().stream().map(hr -> hr.getItem());
    }

}
