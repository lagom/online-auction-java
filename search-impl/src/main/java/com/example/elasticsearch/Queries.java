package com.example.elasticsearch;

/**
 *
 */
public class Queries {

    public static QueryRoot getOpenAuctionsUnderPrice(int maxPrice){
        return new QueryRoot(new BooleanQuery(new MustFilter(new RangeFilter.PriceRange(maxPrice)))) ;
    }


}
