package com.example.elasticsearch;

import com.example.auction.item.api.ItemStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.util.function.Predicate;

/**
 *
 */
interface MatchFilter extends Filter {

    Match getMatch();

    @Value
    class ItemStatusFilter implements MatchFilter {
        Match match;

        @JsonCreator
        public ItemStatusFilter(ItemStatus itemStatus) {
            match = new Match.ItemStatusMatch(itemStatus);
        }


        @Override
        public Predicate<? super IndexedItem> predicate() {
            return match.predicate();
        }
    }
}
