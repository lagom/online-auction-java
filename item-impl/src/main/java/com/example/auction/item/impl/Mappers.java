package com.example.auction.item.impl;

import com.example.auction.item.api.Item;
import com.example.auction.item.api.ItemData;

/**
 *
 */
public class Mappers {

    public static Item toApi(PItem item) {
        ItemData data = toApi(item.getItemData());
        return new Item(
                item.getId(),
                item.getCreator(),
                data,
                item.getPrice(),
                item.getStatus().toItemStatus(),
                item.getAuctionStart(),
                item.getAuctionEnd(),
                item.getAuctionWinner());
    }

    public static ItemData toApi(PItemData details) {
        return new ItemData(
                details.getTitle(),
                details.getDescription(),
                details.getCurrencyId(),
                details.getIncrement(),
                details.getReservePrice(),
                details.getAuctionDuration());
    }

    public static PItemData fromApi(ItemData data) {
        return new PItemData(
                data.getTitle(),
                data.getDescription(),
                data.getCurrencyId(),
                data.getIncrement(),
                data.getReservePrice(),
                data.getAuctionDuration());
    }
}
