package com.example.auction.item.api;

import lombok.Value;
import org.pcollections.PSequence;

/**
 * A partial sequence of elements, with metadata for retrieving additional pages.
 */
@Value
public class PaginatedSequence<T> {
    PSequence<T> items;
    int page;
    int pageSize;
    int count;

    public PaginatedSequence(PSequence<T> items, int page, int pageSize, int count) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.count = count;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean isFirst() {
        return page == 0;
    }

    public boolean isLast() {
        return count <= (page + 1) * pageSize;
    }

    public boolean isPaged() {
        return count > pageSize;
    }

    public int getPageCount() {
        return ((count - 1) / pageSize) + 1;
    }
}
