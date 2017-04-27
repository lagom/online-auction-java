# Running Online Auction in ConductR Sandbox

This guide tells you how to run Online Auction in a simulated production environment, using the ConductR Developer Sandbox: part of the [Lightbend Production Suite](http://www.lightbend.com/platform/production).

Be sure to read the main [`README.md`](../README.md) document before getting started.

You might also want to familiarize yourself with the [Lagom documentation on running in ConductR](https://www.lagomframework.com/documentation/1.3.x/java/ConductR.html).

## Prerequisites

1. Install [Docker](https://www.docker.com/) for your platform
2. Install and run the [ConductR Developer Sandbox](https://www.lightbend.com/product/conductr/developer)

## Run Kafka (with ZooKeeper)

This configuration assumes that Kafka will be available on localhost at port 9092. You can run Kafka and ZooKeeper anyway you like, but it is convenient to use a local Docker container.

This command will download and run [Spotify's Docker image for Kafka and ZooKeeper](https://hub.docker.com/r/spotify/kafka/).

```sh
docker run -p 2181:2181 \
           -p 9092:9092 \
           --env ADVERTISED_HOST=127.0.0.1 \
           --env ADVERTISED_PORT=9092 \
           spotify/kafka
```

Note that this image has a very basic configuration that is useful for development and evaluation purposes, but is not suitable for a real production environment.

## Run Elasticsearch (optional)

To use the [Search Service](search-service.md) functionality, you'll need to run an instance of Elasticsearch on localhost port 9200. This is optional: if you don't run Elasticsearch, searching won't work but other areas of the application will be unaffected.

Again, you can run this however you like, either by downloading and executing Elasticsearch directly (see [`README.md`](../README.md) for instructions) or by running a Docker image, such as the [official Elasticsearch Docker image](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html):

```sh
docker run -p 9200:9200 \
           -e "http.host=127.0.0.1" \
           -e "transport.host=127.0.0.1" \
           docker.elastic.co/elasticsearch/elasticsearch:5.0.2
```

## Start the ConductR Sandbox

Run this command to start the ConductR sandbox:

```sh
sandbox run 2.0.5
```

Refer to the [ConductR Developer Sandbox](https://www.lightbend.com/product/conductr/developer) documentation for the latest version of the sandbox and other details.

## Run Online Auction

Start an interactive `sbt` session and run `install`.

This packages and runs all of the Online Auction services, along with Cassandra and HAProxy, in the ConductR sandbox.

Look in the output for a message like this:

> HAProxy has been started
> By default, your bundles are accessible on:
>   192.168.10.1:9000

This tells you the URL you can use to load Online Auction in your web browser.

See [Using ConductR with sbt](https://www.lagomframework.com/documentation/1.3.x/java/ConductRSbt.html) for more information on the commands available from the sbt console.

## Adapting for Production

This is intended as a demonstration only.

In a real production application, you will need to configure Cassandra, ZooKeeper, Kafka and other required infrastructure appropriately for your environment and expected load. Refer to the documentation for each of these components for details.
