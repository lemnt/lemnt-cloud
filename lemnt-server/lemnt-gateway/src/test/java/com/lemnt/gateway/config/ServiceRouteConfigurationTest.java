package com.lemnt.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.cloud.gateway.filter.factory.StripPrefixGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

/** 验证真实路由谓词及StripPrefix过滤器执行后的服务和路径。 */
class ServiceRouteConfigurationTest {
    @Test
    void servicePrefixesReachActualControllerPathsWithoutChangingQuery() {
        try (var context = new StaticApplicationContext()) {
            context.getBeanFactory().registerSingleton("pathRoutePredicateFactory", new PathRoutePredicateFactory(new WebFluxProperties()));
            context.getBeanFactory().registerSingleton("stripPrefixGatewayFilterFactory", new StripPrefixGatewayFilterFactory());
            var builder = new RouteLocatorBuilder(context);
            var routes = new ArrayList<Route>(new ServiceRouteConfiguration().serviceRoutes(builder).getRoutes().collectList().block());
            routes.addAll(new MemberAuthRouteConfiguration().memberAuthRoutes(builder).getRoutes().collectList().block());
            routes.sort(Comparator.comparingInt(Route::getOrder));
            String[][] cases = {
                    {"/member/member-users", "lemnt-member", "/member-users"},
                    {"/member/growth/levels", "lemnt-member", "/growth/levels"},
                    {"/member/growths/config", "lemnt-member", "/growths/config"},
                    {"/member/groups/page", "lemnt-member", "/groups/page"},
                    {"/member/tags/page", "lemnt-member", "/tags/page"},
                    {"/member/app/users/user", "lemnt-member", "/app/users/user"},
                    {"/member/app/growths/point-records", "lemnt-member", "/app/growths/point-records"},
                    {"/member/app/addresses/default", "lemnt-member", "/app/addresses/default"},
                    {"/member/admin/level-records", "lemnt-member", "/member/admin/level-records"},
                    {"/member/app/auth/login", "lemnt-auth", "/member/app/auth/login"},
                    {"/member/app/auth/refresh-token", "lemnt-auth", "/member/app/auth/refresh-token"},
                    {"/member/app/auth/logout", "lemnt-auth", "/member/app/auth/logout"},
                    {"/auth/sessions/login", "lemnt-auth", "/sessions/login"},
                    {"/system/users/current-info", "lemnt-system", "/users/current-info"},
                    {"/system/areas/tree", "lemnt-system", "/areas/tree"},
                    {"/system/files/app/upload", "lemnt-system", "/files/app/upload"},
                    {"/mall/app/products", "lemnt-mall", "/mall/app/products"},
                    {"/trade/app/orders/NO/receive", "lemnt-trade", "/trade/app/orders/NO/receive"}
            };
            for (var item : cases) {
                var exchange = MockServerWebExchange.from(MockServerHttpRequest.get(item[0] + "?pageNum=2"));
                var route = routes.stream().filter(r -> Boolean.TRUE.equals(Mono.from(r.getPredicate().apply(exchange)).block())).findFirst().orElseThrow();
                assertEquals("lb://" + item[1], route.getUri().toString(), item[0]);
                var forwarded = new AtomicReference<String>();
                if (route.getFilters().isEmpty()) forwarded.set(exchange.getRequest().getURI().getPath());
                else route.getFilters().get(0).filter(exchange, next -> {
                    forwarded.set(next.getRequest().getURI().getPath());
                    assertEquals("pageNum=2", next.getRequest().getURI().getRawQuery());
                    return Mono.empty();
                }).block();
                assertEquals(item[2], forwarded.get(), item[0]);
            }
            for (String path : List.of("/membership/user", "/mallish/products", "/tradeoff/orders", "/systematic/users")) {
                var exchange = MockServerWebExchange.from(MockServerHttpRequest.get(path));
                assertTrue(routes.stream().noneMatch(r -> Boolean.TRUE.equals(Mono.from(r.getPredicate().apply(exchange)).block())));
            }
        }
    }
}
