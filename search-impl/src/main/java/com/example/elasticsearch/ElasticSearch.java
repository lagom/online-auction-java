package com.example.elasticsearch;

import akka.Done;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import com.lightbend.lagom.javadsl.api.transport.Method;
import org.pcollections.PSequence;

import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.named;


/**
 *
 */
public interface ElasticSearch extends  Service {

    ServiceCall<IndexedItem, Done> updateIndex(String index, UUID itemId);

    ServiceCall<Query, PSequence<IndexedItem>> search(String index);

    @Override
    default public Descriptor descriptor() {
        return named("elastic-search")
                //  .withCircuitBreaker()
                .withCalls(
                        Service.restCall(Method.GET, "/:index/_search", this::search),
                        Service.restCall(Method.PUT, "/:index/external/:id", this::updateIndex)
                ).withPathParamSerializer(
                        UUID.class, PathParamSerializers.required("UUID", UUID::fromString, UUID::toString)
                )
                .withAutoAcl(true);
    }
}
