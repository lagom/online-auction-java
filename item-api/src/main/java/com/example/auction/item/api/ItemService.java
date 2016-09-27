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
import org.pcollections.PSequence;

import java.util.Optional;
import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.*;

public interface ItemService extends Service {

  ServiceCall<Item, Item> createItem();

  ServiceCall<Item, Done> updateItem(UUID id);

  ServiceCall<NotUsed, Done> startAuction(UUID id);

  ServiceCall<NotUsed, Item> getItem(UUID id);

  ServiceCall<NotUsed, PSequence<Item>> getItemsForUser(UUID id, Optional<Integer> pageNo, Optional<Integer> pageSize);

  Topic<ItemEvent> itemEvents();

  @Override
  default Descriptor descriptor() {
    return named("item").withCalls(
            pathCall("/api/item", this::createItem),
            restCall(Method.POST, "/api/item/:id/start", this::startAuction),
            pathCall("/api/item/:id", this::getItem),
            pathCall("/api/item?userId&pageNo&pageSize", this::getItemsForUser)
    ).publishing(
            topic("item-ItemEvent", this::itemEvents)
    ).withPathParamSerializer(UUID.class, PathParamSerializers.required("UUID", UUID::fromString, UUID::toString))
            .withHeaderFilter(SecurityHeaderFilter.INSTANCE);
  }
}
