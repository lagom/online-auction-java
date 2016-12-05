package com.example.auction.search.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.deser.PathParamSerializers;
import com.lightbend.lagom.javadsl.api.transport.Method;
import org.pcollections.PSequence;

import java.util.UUID;

import static com.lightbend.lagom.javadsl.api.Service.named;

public interface SearchService extends Service {

    ServiceCall<SearchRequest, SearchResult> search();

    ServiceCall<NotUsed, PSequence<SearchItem>> getUserAuctions(UUID userId);

    ServiceCall<NotUsed, PSequence<SearchItem>> getOpenAuctionsUnderPrice(Integer maxPrice);

    @Override
    default Descriptor descriptor() {

        return named("search").withCalls(
                Service.restCall(Method.GET, "/search", this::search),
                Service.restCall(Method.GET, "/auctions?userId", this::getUserAuctions),
                Service.restCall(Method.GET, "/auctions/:maxPrice", this::getOpenAuctionsUnderPrice)
        ).withPathParamSerializer(
                UUID.class, PathParamSerializers.optional("UUID", UUID::fromString, UUID::toString)
        ).withAutoAcl(true);
    }
}
