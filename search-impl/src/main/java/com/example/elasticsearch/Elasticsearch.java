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
public interface Elasticsearch extends  Service {

    ServiceCall<UpdateIndexItem, Done> updateIndex(String index, UUID itemId);


    ServiceCall<QueryRoot, SearchResult> search(String index);

    @Override
    default public Descriptor descriptor() {
        return named("elastic-search")
                .withCalls(
                        Service.restCall(Method.GET, "/:index/items/_search", this::search),
                        // we are using the ES endpoint for partial updates because the events we index don't
                        // contain all the data we're storing.
                        Service.restCall(Method.POST, "/:index/items/:id/_update", this::updateIndex)
                ).withPathParamSerializer(
                        UUID.class, PathParamSerializers.required("UUID", UUID::fromString, UUID::toString)
                )
                .withAutoAcl(true);
    }
}
