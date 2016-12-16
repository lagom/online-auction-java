package com.example.auction.search.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;
import org.pcollections.PSequence;

// TODO: migrate to PaginatedSequence and reuse code. Needs moving PaginatedSequence to a shared project.
@Value
public final class SearchResult {

    PSequence<SearchItem> items;
    int pageSize;
    int pageNo;
    int numResults;

    @JsonCreator
    public SearchResult(PSequence<SearchItem> items, int pageSize, int pageNo, int numResults) {
        this.items = items;
        this.pageSize = pageSize;
        this.pageNo = pageNo;
        this.numResults = numResults;
    }

}
