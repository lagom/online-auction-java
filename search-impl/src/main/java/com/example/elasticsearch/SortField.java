package com.example.elasticsearch;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public interface SortField {

    SortField score = new ScoreSort();
    SortField auctionEndDescending = new AuctionEndSort();
    SortField priceAscending = new PriceSort();
    SortField status = new StatusSort();

    @JsonSerialize(as=String.class)
    class ScoreSort implements SortField {
        @JsonUnwrapped
        String key = "_score";
    }

    class AuctionEndSort implements SortField {
        @JsonProperty("auctionEnd")
        String auctionEnd = "desc";

        @JsonCreator
        AuctionEndSort() {
        }
    }

    class PriceSort implements SortField {
        @JsonProperty("price")
        String price = "asc";

        @JsonCreator
        PriceSort() {
        }
    }

    class StatusSort implements SortField {
        @JsonProperty("status")
        String status = "asc";

        @JsonCreator
        StatusSort() {
        }
    }
}
