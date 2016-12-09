package com.example.auction.search.api;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import com.lightbend.lagom.javadsl.api.transport.Method;

import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.named;

public interface SearchService extends Service {

    // TODO: pagination
    ServiceCall<SearchRequest, SearchResult> search(int pageNo, int pageSize);

    // TODO: search by category, price, checkboxes -> status.


    @Override
    default Descriptor descriptor() {
        // TODO: add authentication? I don't think searching needs authentication... Hmmm...
        return named("search").withCalls(
                Service.restCall(Method.GET, "/search?pageNo&pageSize", this::search)
        ).withPathParamSerializer(
                UUID.class, PathParamSerializers.optional("UUID", UUID::fromString, UUID::toString)
        ).withAutoAcl(true);
    }
}
