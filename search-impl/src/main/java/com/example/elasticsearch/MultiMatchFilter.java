package com.example.elasticsearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.function.Predicate;

/**
 *
 */
interface MultiMatchFilter extends Filter {

    @Value
    class KeywordsFilter implements MatchFilter {
        @JsonProperty("multi_match")
        Match match;

        @JsonCreator
        public KeywordsFilter(String keywords) {
            match = new Match.KeywordsMatch(keywords);
        }

    }
}
