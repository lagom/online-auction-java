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

/**
 * The bidding services.
 *
 * This services manages all bids and lifecycle events associated with them.
 *
 * An auction is created when an AuctionStarted event is received, then bids can be placed, and when the end date
 * specified in AuctionStarted is reached, this service will published a BiddingFinished event with the winning
 * bidder (if there was one).
 */
public interface BiddingService extends Service {

  /**
   * A place a bid.
   *
   * @param itemId The item to bid on.
   */
  ServiceCall<Bid, BidResult> placeBid(UUID itemId);

  /**
   * Get the bids for an item.
   *
   * @param itemId The item to get the bids for.
   */
  ServiceCall<NotUsed, PSequence<Bid>> getBids(UUID itemId);

  // TopicCall<BidEvent> bidEvents();

  @Override
  default Descriptor descriptor() {
    return named("bidding");
  }
}
