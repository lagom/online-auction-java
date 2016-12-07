package com.example.elasticsearch;

import com.example.auction.item.api.ItemStatus;

import java.util.Optional;

/**
 *
 */
public class Queries {

    public static QueryRoot getOpenAuctionsUnderPrice(int maxPrice) {
        return buildQuery(
                new MatchFilter.ItemStatusFilter(ItemStatus.AUCTION),
                new RangeFilter(new RangeFilterField.PriceRange(maxPrice))
        );
    }

    public static QueryRoot forKeywords(Optional<String> keywords) {
        return buildQuery(
                new MultiMatchFilter.KeywordsFilter(keywords.get())
        );
    }

    // -----------------------------------------------------------------------------------------------

    private static final Filter FORBID_CREATED = new MatchFilter.ItemStatusFilter(ItemStatus.CREATED);

    private static QueryRoot buildQuery(Filter... should) {
        // any search will ignore items on CREATED status.
        return new QueryRoot(new Query(new BooleanQuery(FORBID_CREATED, should)));
    }
}
