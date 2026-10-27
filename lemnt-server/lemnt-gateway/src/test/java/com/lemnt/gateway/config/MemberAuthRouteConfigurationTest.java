package com.lemnt.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MemberAuthRouteConfigurationTest {
    @Test
    void onlyExactAuthenticationPathsRouteToAuthWithoutStrippingPrefix() {
        try (var context = new StaticApplicationContext()) {
            context.getBeanFactory().registerSingleton("pathRoutePredicateFactory",
                    new PathRoutePredicateFactory(
                    new WebFluxProperties()));
            var routes = new MemberAuthRouteConfiguration().memberAuthRoutes(new RouteLocatorBuilder(context))
                .getRoutes().collectList().block();
            assertEquals(1, routes.size());
            var route = routes.get(0);
            assertEquals("lb://lemnt-auth", route.getUri().toString());
            assertTrue(route.getOrder() < 0);
            assertTrue(route.getFilters().isEmpty());
            for (String path : List.of("/member/app/auth/login", "/member/app/auth/refresh-token", "/member/app/auth/logout")) {
                var exchange = MockServerWebExchange.from(MockServerHttpRequest.post(path));
                assertEquals(Boolean.TRUE, Mono.from(route.getPredicate().apply(exchange)).block());
                assertEquals(path, exchange.getRequest().getURI().getPath());
            }
            for (String path : List.of("/member/app/auth/sms-login", "/member/app/auth/social-login",
                    "/member/app/user", "/member/admin/users", "/member/app/auth/login/extra")) {
                var exchange = MockServerWebExchange.from(MockServerHttpRequest.post(path));
                assertEquals(Boolean.FALSE, Mono.from(route.getPredicate().apply(exchange)).block());
            }
        }
    }
}
