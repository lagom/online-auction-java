package com.example.elasticsearch;

import com.example.auction.item.api.ItemStatus;

import java.util.Optional;

/**
 *
 */
public class Queries {

    public static QueryRoot getOpenAuctionsUnderPrice(int maxPrice) {
        return new QueryRoot(new Query( new BooleanQuery(
                new MatchFilter.ItemStatusFilter(ItemStatus.AUCTION),
                new RangeFilter(new RangeFilterField.PriceRange(maxPrice))
        )));
    }


    public static QueryRoot forKeywords(Optional<String> keywords) {
        return new QueryRoot(new Query(new BooleanQuery(
                new MultiMatchFilter.KeywordsFilter(keywords.get())
        )));
    }
}
