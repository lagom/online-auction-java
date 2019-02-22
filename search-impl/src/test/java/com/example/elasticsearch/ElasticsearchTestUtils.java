package com.example.elasticsearch;

import akka.Done;
import akka.NotUsed;
import com.example.auction.search.impl.IndexedStoreImpl;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import com.lightbend.lagom.javadsl.api.transport.Method;

import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.named;


public interface ElasticsearchTestUtils extends Service {

    ServiceCall<NotUsed, Done> deleteIndex();
    ServiceCall<NotUsed, Done> deleteOne(UUID id);
    ServiceCall<NotUsed, Done> refresh();
    ServiceCall<NotUsed, Done> flush();
    ServiceCall<NotUsed, Done> merge();
    ServiceCall<NotUsed, Done> clearCache();

    @Override
    default Descriptor descriptor() {
        return named("elastic-search-test-utils")
                .withCalls(
                        Service.restCall(Method.DELETE, "/" + IndexedStoreImpl.INDEX_NAME , this::deleteIndex),
                        Service.restCall(Method.DELETE, "/" + IndexedStoreImpl.INDEX_NAME + "/items/:id", this::deleteOne),
                        Service.restCall(Method.POST, "/" + IndexedStoreImpl.INDEX_NAME + "/_refresh", this::refresh),
                        Service.restCall(Method.POST, "/" + IndexedStoreImpl.INDEX_NAME + "/_flush", this::flush),
                        Service.restCall(Method.POST, "/" + IndexedStoreImpl.INDEX_NAME + "/_cache/clear", this::clearCache),
                        Service.restCall(Method.POST, "/" + IndexedStoreImpl.INDEX_NAME + "/_forcemerge", this::merge)
                ).withPathParamSerializer(
                        UUID.class, PathParamSerializers.required("UUID", UUID::fromString, UUID::toString)
                ).withAutoAcl(true);
    }

}
