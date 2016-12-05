package com.example.elasticsearch;

import com.example.auction.item.api.ItemStatus;

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


}
