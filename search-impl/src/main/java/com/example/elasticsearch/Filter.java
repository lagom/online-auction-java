package com.example.elasticsearch;

import java.util.function.Predicate;

/**
 *
 */
public interface Filter {
    Predicate<? super IndexedItem> predicate();

}
