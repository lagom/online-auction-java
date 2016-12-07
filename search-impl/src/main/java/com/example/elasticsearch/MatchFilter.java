package com.example.elasticsearch;

import com.example.auction.item.api.ItemStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.function.Predicate;

/**
 *
 */
interface MatchFilter extends Filter {

    @Value
    class ItemStatusFilter implements MatchFilter {
        Match match;

        @JsonCreator
        public ItemStatusFilter(ItemStatus itemStatus) {
            match = new Match.ItemStatusMatch(itemStatus);
        }

    }
}


