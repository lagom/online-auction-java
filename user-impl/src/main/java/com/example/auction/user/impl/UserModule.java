package com.example.auction.user.impl;

import com.example.auction.user.api.UserService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class UserModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindServices(UserService.SERVICE_ID,serviceBinding(UserService.class, UserServiceImpl.class));
    }
}
