# [Item Service](../item-api/src/main/java/com/example/auction/item/api/ItemService.java)


Manages the description and auction status (created, auction, completed, cancelled) of an item.

## Commands handled

### External (user)

* **createItem** - Creates an item - emits **ItemUpdated**.
* **updateItem** - Updates user editable properties of an item, if allowed in the current state (eg, currency can't be updated after auction is started), emits **ItemUpdated**.
* **startAuction** - Starts the auction if current state allows it, emits **AuctionStarted**.
* **cancelAuction** (not supported) - Cancels the auction if current state allows it, emits **AuctionCancelled**.

## Queries handled

* **getItem** - Gets an item by an ID.
* **getItemsForUser** - Gets a list of items in a provided status that are owned by a given user.

## Events emitted

* **AuctionStarted** (public) - When the auction is started, in response to **startAuction**.
* **ItemUpdated** (public) - When user editable fields on an item are updated in response to **createItem** or **updateItem**.
* **AuctionCancelled** (private) - When the auction is cancelled, in response to **cancelAuction**.
* **AuctionFinished** (public) - When the auction is finished, in response to **BiddingFinished**.

Event emitted publicly are published via a broker topic named `item-ItemEvent`.


## Events consumed

* **BidService.BidPlaced** - on every accepted bid, Item service will update a copy of the price of the item. This is a denormalized field, Bid Service is the source of truth.
* **BidService.BiddingFinished** - when Bid Service notifies the auction is over, Item service updates it's copy of the final price and winner (if there was one).