package com.example.auction.search.api;

import org.pcollections.PSequence;

public final class SearchResult {

    private final PSequence<SearchItem> items;
    private final int pageSize;
    private final int pageNo;
    private final int numPages;
}
