/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.example.auction.item.api;

import akka.Done;
import akka.NotUsed;
import com.example.auction.security.SecurityHeaderFilter;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import com.lightbend.lagom.javadsl.api.transport.Method;

import java.util.Optional;
import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 * The item service.
 * Manages the lifecycle of items, as well properties on them.
 */
public interface ItemService extends Service {

    /**
     * Create an item.
     *
     * @return The created item with its ID populated.
     */
    ServiceCall<Item, Item> createItem();

    /**
     * Update an item.
     *
     * @param id The ID of the item to update.
     * @return Done.
     */
    ServiceCall<Item, UpdateItemResult> updateItem(UUID id);

    /**
     * Start an auction for an item.
     *
     * @param id The id of the item to start the auction for.
     * @return Done if the auction was started.
     */
    ServiceCall<NotUsed, Done> startAuction(UUID id);

    /**
     * Get an item with the given ID.
     *
     * @param id The ID of the item to get.
     * @return The item.
     */
    ServiceCall<NotUsed, Item> getItem(UUID id);

    /**
     * Get a list of items for the given user.
     *
     * @param id       The ID of the user.
     * @param status   The status of items to return.
     * @param pageNo   The page number, starting from zero.
     * @param pageSize The number of items to return per page.
     * @return The sequence of items.
     */
    ServiceCall<NotUsed, PaginatedSequence<ItemSummary>> getItemsForUser(
            UUID id, ItemStatus status, Optional<Integer> pageNo, Optional<Integer> pageSize);

    /**
     * The item events stream.
     */
    Topic<ItemEvent> itemEvents();



    @Override
    default Descriptor descriptor() {
        return named("item").withCalls(
                pathCall("/api/item", this::createItem),
                restCall(Method.POST, "/api/item/:id/start", this::startAuction),
                pathCall("/api/item/:id", this::getItem),
                pathCall("/api/item/:id/update", this::updateItem),
                pathCall("/api/item?userId&status&pageNo&pageSize", this::getItemsForUser)
        ).publishing(
                topic("item-ItemEvent", this::itemEvents)
        ).withPathParamSerializer(
                UUID.class, PathParamSerializers.required("UUID", UUID::fromString, UUID::toString)
        ).withPathParamSerializer(
                ItemStatus.class, PathParamSerializers.required("ItemStatus", ItemStatus::valueOf, ItemStatus::toString)
        ).withPathParamSerializer(
                UpdateItemResultCodes.class, PathParamSerializers.required("UpdateItemResultCode", UpdateItemResultCodes::valueOf, UpdateItemResultCodes::toString)
        ).withHeaderFilter(SecurityHeaderFilter.INSTANCE);
    }

}
