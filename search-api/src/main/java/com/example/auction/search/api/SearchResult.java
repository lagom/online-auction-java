package com.example.auction.search.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;
import org.pcollections.PSequence;

@Value
public final class SearchResult {

    private final PSequence<SearchItem> items;
    private final int pageSize;
    private final int pageNo;
    private final int numPages;

    @JsonCreator
    public SearchResult(PSequence<SearchItem> items, int pageSize, int pageNo, int numPages) {
        this.items = items;
        this.pageSize = pageSize;
        this.pageNo = pageNo;
        this.numPages = numPages;
    }
}
