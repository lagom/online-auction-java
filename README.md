# INTRO

This is a sample app of the Lagom Framework.

## Running: Prerequisites

You will need to download and run an Elastisearch server:

```
curl -L -O https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.0.2.tar.gz
tar -xvf elasticsearch-5.0.2.tar.gz
cd elasticsearch-5.0.2/bin
./elasticsearch
```
## Running

On another terminal, clone this repo and run the sample app using the command `sbt runAll`:

```
git clone git@github.com:lagom/online-auction-java.git
cd online-auction-java
sbt runAll
```
## Importing into IDEs

* To import the online-auction-javaproject into IntellijIDEA you can have a look at [The Lagom Documentation for IDEA](http://www.lagomframework.com/documentation/1.3.x/java/IntellijSbtJava.html)
* To import the online-auction-javaproject into Eclipse you can have a look at [The Lagom Documentation for Eclipse](http://www.lagomframework.com/documentation/1.3.x/java/EclipseSbt.html)
* You will also need to add Lambok plugin in your IDE for annotations. Take a look at [The Lagom Documentation for Lambok](http://www.lagomframework.com/documentation/1.3.x/java/Immutable.html#Lombok)

# Auction system - System architecture

The auction system is the sum of 5 micro-services and a web gateway:

* **[Item Service](docs/item-service.md)**: Manages the description and auction status (created, auction, completed, cancelled) of an item.
* **[Bidding service](docs/bidding-service.md)**: Manages bids on items.
* **[Search service](docs/search-service.md)**: Handles all item searching.
* **[Transaction service](docs/transaction-service.md)**: Handles the transaction of negotiating delivery info and making payment of an item that has completed an auction.
* **user-service**: a convenience service to stub user management. Don't use any code in `user-service` as reference on how to create a secure user management micro-service.
* **web-gateway**: a [Play](https://www.playframework.com/) application providing web UI and acting as gateway to all previously described services.

Check the docs for each service for details on the **commands** and **queries** it serves as well as events the service **emits** and events it **consumes** from the [Message Broker](http://www.lagomframework.com/documentation/1.3.x/java/MessageBroker.html#Message-Broker-Support).
