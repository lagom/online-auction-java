/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.example.auction.item.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.PSequence;

import java.util.Optional;
import java.util.UUID;

public interface ItemService extends Service {

  ServiceCall<Item, Done> createItem();

  ServiceCall<Item, Done> updateItem(UUID id);

  ServiceCall<NotUsed, Done> startAuction(UUID id);

  ServiceCall<NotUsed, Item> getItem(UUID id);

  ServiceCall<NotUsed, PSequence<Item>> getItemsForUser(UUID id, Optional<Integer> pageNo, Optional<Integer> pageSize);

  // TopicCall<ItemEvent> itemEvents();

  @Override
  default Descriptor descriptor() {
    return named("item");
  }
}
