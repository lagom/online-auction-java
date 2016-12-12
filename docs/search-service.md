
# Search service

Handles all item searching.

The implemetation is leveraging an Elasticsearch server to store and then search Items. To access Elasticsearch this service uses the [HTTP API](https://www.elastic.co/guide/en/elasticsearch/reference/current/docs.html) so this is an example of wrapping a 3rd party HTTP API on a [Lagom Descriptor](http://www.lagomframework.com/documentation/).

## Queries handled

* **search** - Search for items under auction (or completed) matching a given criteria.

## Events consumed

* **ItemService.ItemUpdated** - Creates or updates the details for an item in the search index
* **ItemService.AuctionStarted** - Updates the status for an item to started
* **ItemService.AuctionFinished** - Updates the Indexed document with the payload on the event. The payload is the projection of the Item once the Auction is over. This event will overwrite all fields in the index and is considered the source of truth for a completed auction. 
* **BidService.BidPlaced** - Updates the current price for an item. If the item doesn't exist in the index, a new item is created expecting an `ItemService.ItemUpdated` and a `ItemService.AuctionStarted` to eventually arrive.
* **BidService.BiddingFinished** - Updates the current price for an item, if it exists in the index

All events must include an `itemId`. Data in the index is updated incrementally so that each field on the indexed documents is only updated by the events coming from the service where that field originated. E.g. only those events incoming from `ItemService` may update `title` or `description`. The only exception to this rule is `ItemService.AuctionFinished` which may update the field `price`, otherwise only updated by events from `BidService`.