package com.example.elasticsearch;

import com.example.auction.item.api.ItemStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.function.Predicate;

/**
 *
 */
interface Match extends Filter {

    @Value
    class ItemStatusMatch implements Match {
        ItemStatus status;

        @JsonCreator
        public ItemStatusMatch(ItemStatus status) {
            this.status = status;
        }

        @Override
        public Predicate<? super IndexedItem> predicate() {
            return item -> item.getStatus().map(st -> st.equals(status)).orElse(false);
        }
    }

}
