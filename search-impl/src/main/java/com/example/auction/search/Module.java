package com.example.auction.search;

import com.example.auction.bidding.api.BiddingService;
import com.example.auction.item.api.ItemService;
import com.example.auction.search.api.SearchService;
import com.example.auction.search.impl.BrokerEventConsumer;
import com.example.auction.search.impl.IndexedStoreImpl;
import com.example.auction.search.impl.SearchServiceImpl;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import org.taymyr.lagom.elasticsearch.document.ElasticDocument;
import org.taymyr.lagom.elasticsearch.search.ElasticSearch;

/**
 *
 */
public class Module extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(SearchService.class, SearchServiceImpl.class);

        bindClient(BiddingService.class);
        bindClient(ItemService.class);

        bindClient(ElasticSearch.class);
        bindClient(ElasticDocument.class);

        bind(IndexedStore.class).to(IndexedStoreImpl.class);

        bind(BrokerEventConsumer.class).asEagerSingleton();

    }
}
