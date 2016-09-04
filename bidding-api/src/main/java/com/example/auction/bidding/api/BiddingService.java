/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package com.example.auction.bidding.api;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.pathCall;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.PSequence;

import java.util.UUID;

public interface BiddingService extends Service {

  ServiceCall<Bid, Done> placeBid(UUID itemId);

  ServiceCall<NotUsed, PSequence<Bid>> getBids(UUID itemId);

  TopicCall<BidEvent> bidEvents();

  @Override
  default Descriptor descriptor() {
  }
}
