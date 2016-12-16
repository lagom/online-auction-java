package com.example.core;

import akka.Done;
import akka.actor.ActorRef;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.broker.Subscriber;
import com.lightbend.lagom.javadsl.api.broker.Topic;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

// WIP
public class TopicStub<T> implements Topic<T> {

    private final Materializer materializer;
    private ActorRef sourceActor;

    public TopicStub(Materializer materializer) {
        this.materializer = materializer;
    }

    @Override
    public Topic.TopicId topicId() {
        return null;
    }

    @Override
    public Subscriber<T> subscribe() {
        return new Subscriber<T>() {
            @Override
            public Subscriber<T> withGroupId(String groupId) throws IllegalArgumentException {
                return null;
            }

            @Override
            public Source<T, ?> atMostOnceSource() {
                return null;
            }

            @Override
            public CompletionStage<Done> atLeastOnce(Flow<T, Done, ?> flow) {
                Pair<ActorRef, CompletionStage<Done>> pair = Source.<T>actorRef(1, OverflowStrategy.fail())
                        .via(flow)
                        .toMat(Sink.ignore(), Keep.both())
                        .run(materializer);
                sourceActor = pair.first();
                return pair.second();
            }
        };
    }

    public Supplier<ActorRef> actorSupplier() {
        return () -> sourceActor;
    }
}