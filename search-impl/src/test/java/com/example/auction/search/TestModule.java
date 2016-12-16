package com.example.auction.search;

import com.example.elasticsearch.ElasticsearchTestUtils;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;


public class TestModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindClient(ElasticsearchTestUtils.class);
    }
}
