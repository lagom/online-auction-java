package com.example.auction.search.api;

import java.time.Instant;
import java.util.UUID;

public final class SearchItem {

    private final UUID id;
    private final UUID creator;
    private final String title;
    private final String description;
    private final UUID categoryId;
    private final String currencyId;
    private final int price;
    private final Instant auctionStart;
    private final Instant auctionEnd;

}
