package com.example.auction.security;

import com.lightbend.lagom.javadsl.api.security.ServicePrincipal;
import com.lightbend.lagom.javadsl.api.security.UserAgentHeaderFilter;
import com.lightbend.lagom.javadsl.api.transport.HeaderFilter;
import com.lightbend.lagom.javadsl.api.transport.RequestHeader;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

public final class SecurityHeaderFilter implements HeaderFilter {

    private SecurityHeaderFilter() {}

    public static final HeaderFilter INSTANCE = HeaderFilter.composite(new SecurityHeaderFilter(), new UserAgentHeaderFilter());

    @Override
    public RequestHeader transformClientRequest(RequestHeader request) {
        if (request.principal().isPresent()) {
            Principal principal = request.principal().get();
            if (principal instanceof UserPrincipal) {
                return request.withHeader("User-Id", ((UserPrincipal) principal).getUserId().toString());
            }
        }
        return request;
    }

    @Override
    public RequestHeader transformServerRequest(RequestHeader request) {
        Optional<String> userId = request.getHeader("User-Id");
        if (userId.isPresent()) {
            Optional<ServicePrincipal> service = request.principal()
                    .filter(p -> p instanceof ServicePrincipal)
                    .map(p -> (ServicePrincipal) p);

            UUID userUuid = UUID.fromString(userId.get());

            return request.withPrincipal(UserPrincipal.of(userUuid, service));
        }
        return request;
    }

    @Override
    public ResponseHeader transformServerResponse(ResponseHeader response, RequestHeader request) {
        return response;
    }

    @Override
    public ResponseHeader transformClientResponse(ResponseHeader response, RequestHeader request) {
        return response;
    }
}
