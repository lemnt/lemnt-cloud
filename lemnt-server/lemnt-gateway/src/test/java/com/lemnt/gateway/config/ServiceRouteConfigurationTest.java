package com.lemnt.gateway.config;

import com.lemnt.common.core.utils.JsonUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.StripPrefixGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.MethodRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 对原端点逐一执行真实方法/路径谓词及过滤器，不依赖业务RPC或真实中间件。
 */
class ServiceRouteConfigurationTest {
    private AnnotationConfigApplicationContext context;
    private List<Route> routes;

    @BeforeEach
    void setup() {
        context = new AnnotationConfigApplicationContext();
        context.registerBean(
            PathRoutePredicateFactory.class,
            () -> new PathRoutePredicateFactory(new WebFluxProperties()));
        context.registerBean(MethodRoutePredicateFactory.class, MethodRoutePredicateFactory::new);
        context.registerBean(
            StripPrefixGatewayFilterFactory.class, StripPrefixGatewayFilterFactory::new);
        context.registerBean(RouteLocatorBuilder.class, () -> new RouteLocatorBuilder(context));
        context.registerBean(
            ConfigurationService.class,
            () ->
                new ConfigurationService(
                    context,
                    () -> DefaultConversionService.getSharedInstance(),
                    () -> null));
        // 动态配置中的默认过滤器不能改变这些原DSL路由的业务前缀。
        context.registerBean(
            GatewayProperties.class,
            () -> {
                var properties = new GatewayProperties();
                properties.setDefaultFilters(List.of(new FilterDefinition("StripPrefix=1")));
                return properties;
            });
        context.register(ServiceRouteConfiguration.class, MemberAuthRouteConfiguration.class);
        context.refresh();
        routes = new ArrayList<>();
        for (var locator : context.getBeansOfType(RouteLocator.class).values()) {
            routes.addAll(locator.getRoutes().collectList().block());
        }
        routes.sort(Comparator.comparingInt(Route::getOrder));
    }

    @AfterEach
    void close() {
        context.close();
    }

    @TestFactory
    Stream<DynamicTest> allOriginalEndpointsReachExactlyOneOwner() throws Exception {
        var endpoints = endpoints();
        assertEquals(313, endpoints.size());
        return endpoints.stream()
            .map(
                endpoint ->
                    DynamicTest.dynamicTest(
                        endpoint.method() + " " + endpoint.path(),
                        () -> {
                            var exchange =
                                request(
                                    endpoint.method(),
                                    concrete(endpoint.path()));
                            var route = unique(exchange);
                            assertEquals(
                                "lb://lemnt-" + endpoint.owner(),
                                route.getUri().toString());
                            assertEquals(-8, route.getOrder());
                            assertTrue(
                                route.getFilters().isEmpty(),
                                "业务端点不得StripPrefix或改写请求");
                            assertForwarded(
                                route, exchange, concrete(endpoint.path()));
                        }));
    }

    /**
     * 保留GET隐式HEAD和原服务OPTIONS探测，浏览器预检仍在路由之前结束。
     */
    @Test
    void headOptionsAndCorsPreflightRemainCompatible() throws Exception {
        for (var endpoint : endpoints()) {
            if ("GET".equals(endpoint.method())) {
                assertEquals(
                    "lb://lemnt-" + endpoint.owner(),
                    unique(request("HEAD", concrete(endpoint.path()))).getUri().toString());
            }
            assertEquals(
                "lb://lemnt-" + endpoint.owner(),
                unique(request("OPTIONS", concrete(endpoint.path()))).getUri().toString());
        }
        for (String path :
            List.of(
                "/mall/app/products",
                "/mall/app/cart",
                "/mall/app/point-mall/orders",
                "/trade/app/orders/123/payment")) {
            var exchange =
                MockServerWebExchange.from(
                    MockServerHttpRequest.options("http://gateway.example" + path)
                        .header(HttpHeaders.ORIGIN, "https://shop.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"));
            new CorsConfig()
                .corsFilter(List.of("https://shop.example"))
                .filter(
                    exchange,
                    next -> {
                        fail("预检不能转发到业务服务");
                        return Mono.empty();
                    })
                .block();
            assertEquals(
                "https://shop.example",
                exchange.getResponse()
                    .getHeaders()
                    .getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
        }
    }

    @Test
    void servicePrefixesReachActualControllerPathsWithoutChangingQuery() {
        String[][] cases = {
            {"GET", "/member/member-users", "lemnt-member", "/member-users"},
            {"GET", "/member/growth/levels", "lemnt-member", "/growth/levels"},
            {"GET", "/member/growths/config", "lemnt-member", "/growths/config"},
            {"GET", "/member/groups/page", "lemnt-member", "/groups/page"},
            {"GET", "/member/tags/page", "lemnt-member", "/tags/page"},
            {"GET", "/member/app/users/user", "lemnt-member", "/app/users/user"},
            {
                "GET",
                "/member/app/growths/point-records",
                "lemnt-member",
                "/app/growths/point-records"
            },
            {"GET", "/member/app/addresses/default", "lemnt-member", "/app/addresses/default"},
            {"GET", "/member/admin/level-records", "lemnt-member", "/member/admin/level-records"},
            {"GET", "/member/app/auth/login", "lemnt-auth", "/member/app/auth/login"},
            {
                "GET",
                "/member/app/auth/refresh-token",
                "lemnt-auth",
                "/member/app/auth/refresh-token"
            },
            {"GET", "/member/app/auth/logout", "lemnt-auth", "/member/app/auth/logout"},
            {"GET", "/auth/sessions/login", "lemnt-auth", "/sessions/login"},
            {"GET", "/system/users/current-info", "lemnt-system", "/users/current-info"},
            {"GET", "/system/areas/tree", "lemnt-system", "/areas/tree"},
            {"GET", "/system/files/app/upload", "lemnt-system", "/files/app/upload"},
            {"GET", "/mall/app/products", "lemnt-product", "/mall/app/products"},
            {"POST", "/trade/app/orders/NO/receive", "lemnt-order", "/trade/app/orders/NO/receive"}
        };
        for (var item : cases) {
            var exchange = request(item[0], item[1]);
            // 会员认证精确规则优先于既有/member通用规则，保持原排序。
            var matched = matches(exchange);
            var route = matched.get(0);
            assertEquals("lb://" + item[2], route.getUri().toString(), item[1]);
            assertForwarded(route, exchange, item[3]);
        }
        for (String path :
            List.of(
                "/membership/user",
                "/mallish/products",
                "/tradeoff/orders",
                "/systematic/users")) {
            assertTrue(matches(request("GET", path)).isEmpty());
        }
    }

    @Test
    void unknownPathsUnsupportedMethodsAndPrefixLookalikesHaveNoCommerceFallback()
        throws Exception {
        for (var endpoint : endpoints()) {
            assertTrue(matches(request("PATCH", concrete(endpoint.path()))).isEmpty());
            assertTrue(matches(request("TRACE", concrete(endpoint.path()))).isEmpty());
        }
        for (String path :
            List.of(
                "/mall/unknown-resource",
                "/trade/unknown-resource",
                "/mall/app/products/123/extra",
                "/trade/app/orders/123/payment/extra")) {
            assertTrue(matches(request("GET", path)).isEmpty(), path);
        }
        assertTrue(
            routes.stream()
                .noneMatch(
                    r ->
                        List.of("lb://lemnt-mall", "lb://lemnt-trade")
                            .contains(r.getUri().toString())));
    }

    private List<Endpoint> endpoints() throws Exception {
        return JsonUtils.toList(
            new ClassPathResource("contracts/gateway-endpoint-contract.json")
                .getContentAsString(StandardCharsets.UTF_8),
            Endpoint.class);
    }

    private MockServerWebExchange request(String method, String path) {
        return MockServerWebExchange.from(
            MockServerHttpRequest.method(
                HttpMethod.valueOf(method),
                path + "?pageNum=2&keyword=%E4%B8%AD%E6%96%87&ids=1%2C2"));
    }

    private String concrete(String path) {
        return path.replaceAll("\\{[^}]+}", "123");
    }

    private List<Route> matches(ServerWebExchange exchange) {
        return routes.stream()
            .filter(
                r ->
                    Boolean.TRUE.equals(
                        Mono.from(r.getPredicate().apply(exchange)).block()))
            .toList();
    }

    private Route unique(ServerWebExchange exchange) {
        var matched = matches(exchange);
        assertEquals(
            1,
            matched.size(),
            exchange.getRequest().getMethod() + " " + exchange.getRequest().getPath());
        return matched.get(0);
    }

    private void assertForwarded(Route route, ServerWebExchange exchange, String path) {
        var forwarded = new AtomicReference<ServerWebExchange>();
        GatewayFilterChain chain =
            next -> {
                forwarded.set(next);
                return Mono.empty();
            };
        var filters = route.getFilters();
        for (int i = filters.size() - 1; i >= 0; i--) {
            var filter = filters.get(i);
            var next = chain;
            chain = current -> filter.filter(current, next);
        }
        chain.filter(exchange).block();
        assertEquals(path, forwarded.get().getRequest().getURI().getPath());
        assertEquals(
            exchange.getRequest().getURI().getRawQuery(),
            forwarded.get().getRequest().getURI().getRawQuery());
        assertEquals(exchange.getRequest().getMethod(), forwarded.get().getRequest().getMethod());
    }

    public record Endpoint(
        String method,
        String path,
        String owner,
        String permissions,
        String source,
        Integer sourceLine) {
    }
}
