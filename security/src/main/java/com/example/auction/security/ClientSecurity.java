package com.example.auction.security;

import com.lightbend.lagom.javadsl.api.security.ServicePrincipal;
import com.lightbend.lagom.javadsl.api.transport.RequestHeader;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class ClientSecurity {

    /**
     * Authenticate a client request.
     */
    public static final Function<RequestHeader, RequestHeader> authenticate(UUID userId) {
        return request -> {
            Optional<ServicePrincipal> service = request.principal()
                    .filter(p -> p instanceof ServicePrincipal)
                    .map(p -> (ServicePrincipal) p);

            return request.withPrincipal(UserPrincipal.of(userId, service));
        };
    }

}
