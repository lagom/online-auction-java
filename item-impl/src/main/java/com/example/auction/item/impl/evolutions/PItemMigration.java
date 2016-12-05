package com.example.auction.item.impl.evolutions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lightbend.lagom.serialization.JacksonJsonMigration;

/**
 *
 */
public class PItemMigration extends JacksonJsonMigration {
    @Override
    public int currentVersion() {
        return 2;
    }

    @Override
    public JsonNode transform(int fromVersion, JsonNode json) {
        ObjectNode root = (ObjectNode) json;
        if (fromVersion <= 1) {
            ObjectNode itemDetails = root.with("itemData");
            itemDetails.set("title", root.get("title"));
            itemDetails.set("description", root.get("description"));
            itemDetails.set("currencyId", root.get("currencyId"));
            itemDetails.set("increment", root.get("increment"));
            itemDetails.set("reservePrice", root.get("reservePrice"));
            itemDetails.set("auctionDuration", root.get("auctionDuration"));
            root.remove("title");
            root.remove("description");
            root.remove("currencyId");
            root.remove("increment");
            root.remove("reservePrice");
            root.remove("auctionDuration");
        }
        return root;
    }
}
