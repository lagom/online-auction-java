[<img src="docs/logo.png" alt="Lagom" width="50%">](https://github.com/lagom/lagom)

[![Gitter](https://img.shields.io/gitter/room/gitterHQ/gitter.svg)](https://gitter.im/lagom/lagom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [<img src="https://travis-ci.org/lagom/online-auction-java.svg?branch=master"/>](https://travis-ci.org/lagom/online-auction-java)

# Introduction

Lagom is a Swedish word meaning just right, sufficient. Microservices are about creating services that are just the right size, that is, they have just the right level of functionality and isolation to be able to adequately implement a scalable and resilient system.

Lagom focuses on ensuring that your application realises the full potential of the [Reactive Manifesto](http://reactivemanifesto.org/), while delivering a high productivity development environment, and seamless production deployment experience.

This is a **sample Java auction system** using the Lagom Framework. A [Scala version](https://github.com/lagom/online-auction-scala) of the auction system is also available.

When you run the online auction, you access the interface with a browser. You can create user accounts and items for auction. Once the items are available, you can bid.


## Prerequisites
To download and run the online auction example you will need:
* An active internet connection
* sbt
* Git

To use the online auction's search facility, you will also need Elasticsearch, which acts as the search database. You can run the example without Elasticsearch, but the search will not work.

1. To install sbt, refer to the content for your operating system (OS):

   * [The SBT Documentation for MacOS](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Mac.html)
   * [The SBT Documentation for Windows OS](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Windows.html)
   * [The SBT Documentation for Linux OS](http://www.scala-sbt.org/0.13/docs/Installing-sbt-on-Linux.html)

1. Download Elasticsearch server. For example, use a console that supports the `curl` and `tar` commands and enter the following commands one at a time:

     1. `curl -L -O https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.0.2.tar.gz`
     1. `tar -xvf elasticsearch-5.0.2.tar.gz`

## Running in development mode
To run the online auction example on your local machine:

1. Clone the online auction github repository to your local machine.
1. Open a terminal, change into the Elasticsearch `bin` directory and start Elastic search. For example:
        1. `cd elasticsearch-5.0.2/bin`
        1. `elasticsearch`

1. Open another terminal and change to the top-level directory of the cloned online auction repository. For example:
   `cd online-auction-java`

1. Run the sample app using the command `sbt runAll`.
1. Open a browser and enter:
   `localhost:9000`

## Exercising the example

To simulate an auction, you'll need to create at least two users and one item. Once created, you can bid on the item. By using different browsers, you can log in as different users and bid on the same item.


## Running: Kubernetes

This project uses [Lightbend's Platform Tooling](https://developer.lightbend.com/docs/reactive-platform-tooling/latest/) to
simplify deployment to [Kubernetes](https://kubernetes.io/).

Refer to [KUBERNETES.md](KUBERNETES.md) for more information on this process.


## Importing into IDEs (optional)

* To import the online-auction-java project into IntellijIDEA you can have a look at [The Lagom Documentation for IDEA](http://www.lagomframework.com/documentation/1.3.x/java/IntellijSbtJava.html)
* To import the online-auction-javaproject into Eclipse you can have a look at [The Lagom Documentation for Eclipse](http://www.lagomframework.com/documentation/1.3.x/java/EclipseSbt.html)
* You will also need to add Lombok plugin in your IDE for annotations. Take a look at [The Lagom Documentation for Lombok](http://www.lagomframework.com/documentation/1.3.x/java/Immutable.html#Lombok)

## Displaying inline instructions (optional)

To get a better understanding of what can be done at each step of the application, inline instructions are displayed on the web UI. To disable these instructions, go to **[application.conf](web-gateway/conf/application.conf)** in the web-gateway micro-service and set **online-auction.instruction.show** to false.

# Online auction system architecture

The auction system is the sum of 5 micro-services and a web gateway:

* **[Item Service](docs/item-service.md)**: Manages the description and auction status (created, auction, completed, cancelled) of an item.
* **[Bidding service](docs/bidding-service.md)**: Manages bids on items.
* **[Search service](docs/search-service.md)**: Handles all item searching.
* **[Transaction service](docs/transaction-service.md)**: Handles the transaction of negotiating delivery info and making payment of an item that has completed an auction.
* **user-service**: a convenience service to stub user management. Don't use any code in `user-service` as reference on how to create a secure user management micro-service.
* **web-gateway**: a [Play](https://www.playframework.com/) application providing web UI and acting as gateway to all previously described services.

Check the docs for each service for details on the **commands** and **queries** it serves as well as events the service **emits** and events it **consumes** from the [Message Broker](http://www.lagomframework.com/documentation/1.3.x/java/MessageBroker.html#Message-Broker-Support).

## Good to know

- Hello World seed: For a simple, gentler, introduction to Lagom with Java, have a look at the Hello World Lagom archetype using [Maven](https://www.lagomframework.com/get-started-java-maven.html) or Giter8 lagom template  with [sbt](https://www.lagomframework.com/get-started-java-sbt.html).

- Getting help: If you have any troubles and need help, feel free to ask in the [Gitter channel](https://gitter.im/lagom/lagom)
