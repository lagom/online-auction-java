package com.example.elasticsearch;

import com.example.auction.item.api.ItemStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
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
            return item -> item
                    .getStatus()
                    .map(
                            st -> st.equals(status)
                    ).orElse(false);
        }
    }


    @Value
    class KeywordsMatch implements Match {
        String query;
        String[] fields = new String[]{"title", "description"};

        @JsonCreator
        public KeywordsMatch(String keywords) {
            this.query = keywords;
        }

        @Override
        public Predicate<? super IndexedItem> predicate() {
            return item -> item.getKeywords().contains(query);
        }
    }


}
