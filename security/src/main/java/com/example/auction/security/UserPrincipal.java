package com.example.auction.security;

import com.lightbend.lagom.javadsl.api.security.ServicePrincipal;

import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

public class UserPrincipal implements Principal {

    private final UUID userId;

    private UserPrincipal(UUID userId) {
        this.userId = userId;
    }

    @Override
    public String getName() {
        return userId.toString();
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean implies(Subject subject) {
        return false;
    }

    public static UserPrincipal of(UUID userId, Optional<ServicePrincipal> service) {
        if (service.isPresent()) {
            return new UserServicePrincipal(userId, service.get());
        } else {
            return new UserPrincipal(userId);
        }
    }

    private static class UserServicePrincipal extends UserPrincipal implements ServicePrincipal {
        private final ServicePrincipal service;

        public UserServicePrincipal(UUID userId, ServicePrincipal service) {
            super(userId);
            this.service = service;
        }

        @Override
        public String serviceName() {
            return service.serviceName();
        }

        @Override
        public boolean authenticated() {
            return service.authenticated();
        }
    }
}
