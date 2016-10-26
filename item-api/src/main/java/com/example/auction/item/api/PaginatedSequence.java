package com.example.auction.item.api;

import org.pcollections.PSequence;

/**
 * A partial sequence of elements, with metadata for retrieving additional pages.
 */
public class PaginatedSequence<T> {
    private final PSequence<T> items;
    private final int page;
    private final int pageSize;
    private final int count;

    public PaginatedSequence(PSequence<T> items, int page, int pageSize, int count) {
        this.items = items;
        this.page = page;
        this.pageSize = pageSize;
        this.count = count;
    }

    public PSequence<T> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getCount() {
        return count;
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
