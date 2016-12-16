package com.example.elasticsearch;

import akka.Done;
import akka.NotUsed;
import com.example.auction.search.impl.IndexedStoreImpl;
import com.example.auction.search.impl.SearchServiceImpl;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import com.lightbend.lagom.javadsl.api.transport.Method;

import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.named;


/**
 *
 */
public interface ElasticsearchTestUtils extends Service {

    ServiceCall<NotUsed, Done> deleteIndex();

    @Override
    default Descriptor descriptor() {
        return named("elastic-search-test-utils")
                .withCalls(
                        Service.restCall(Method.DELETE, "/"+ IndexedStoreImpl.INDEX_NAME, this::deleteIndex)
                )
                .withAutoAcl(true);
    }
}
