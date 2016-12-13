# [Bidding service](../bidding-api/src/main/java/com/example/auction/bidding/api/BiddingService.java)

Manages bids on items.

## Commands handled

### External (user)

* **placeBid** - Places a bid, if the bid is greater than the current bid, emits **BidPlaced**.

### Internal

* **finishBidding** - Triggered by scheduled task that polls a read side view of auctions to finish, emits **BiddingFinished**

## Queries handled

* **getBids** - Gets all the bids for an item.

## Events emitted

* **BidPlaced** - When a bid is placed, in response to **placeBid**.
* **BiddingFinished** - When bidding has finished, in response to **finishBidding**.

Event emitted publicly are published via a broker topic named `bidding-BidEvent`.

## Events consumed

* **ItemService.AuctionStarted** - Creates a new auction for the item
* **ItemService.AuctionCancelled** - Completes an auction prematurely
