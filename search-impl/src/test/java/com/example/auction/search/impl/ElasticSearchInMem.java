package com.example.auction.search.impl;

import akka.Done;
import com.example.elasticsearch.IndexedItem;
import com.example.elasticsearch.ElasticSearch;
import com.example.elasticsearch.Query;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.elasticsearch.bootstrap.EmbeddedElasticSearchServer;
import org.pcollections.PSequence;

import java.util.UUID;

/**
 *
 */
public class ElasticSearchInMem implements ElasticSearch {

    private EmbeddedElasticSearchServer es;

    @Override
    public ServiceCall<IndexedItem, Done> updateIndex(String index, UUID itemId) {
        return null;
    }

    @Override
    public ServiceCall<Query, PSequence<IndexedItem>> search(String index) {
        return null;
    }


}
