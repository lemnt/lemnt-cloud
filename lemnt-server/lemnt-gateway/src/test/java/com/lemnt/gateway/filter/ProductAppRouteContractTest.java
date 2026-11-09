package com.lemnt.gateway.filter;

import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.model.SecurityUserDetail;
import com.lemnt.common.security.service.TokenService;
import com.lemnt.gateway.config.ServiceRouteConfiguration;
import com.lemnt.gateway.config.properties.IgnoreWhiteProperties;
import com.lemnt.gateway.utils.ServletUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.factory.StripPrefixGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.MethodRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 生产路由谓词与统一鉴权联合验证，防止地址正确但身份域错误。 */
class ProductAppRouteContractTest {
    private AnnotationConfigApplicationContext context;
    private List<Route> routes;
    private AuthFilter filter;
    private TokenService tokens;

    @BeforeEach
    void setup() {
        context = new AnnotationConfigApplicationContext();
        context.registerBean(
            PathRoutePredicateFactory.class,
            () -> new PathRoutePredicateFactory(new WebFluxProperties()));
        context.registerBean(MethodRoutePredicateFactory.class, MethodRoutePredicateFactory::new);
        context.registerBean(
            StripPrefixGatewayFilterFactory.class, StripPrefixGatewayFilterFactory::new);
        context.registerBean(
            ConfigurationService.class,
            () -> new ConfigurationService(
                context, () -> DefaultConversionService.getSharedInstance(), () -> null));
        context.registerBean(
            GatewayProperties.class,
            () -> {
                var properties = new GatewayProperties();
                properties.setDefaultFilters(List.of(new FilterDefinition("StripPrefix=1")));
                return properties;
            });
        context.refresh();
        RouteLocator locator = new ServiceRouteConfiguration().commerceRoutes(
            List.of(context.getBean(PathRoutePredicateFactory.class),
                context.getBean(MethodRoutePredicateFactory.class)),
            List.of(context.getBean(StripPrefixGatewayFilterFactory.class)),
            context.getBean(ConfigurationService.class));
        routes = locator.getRoutes().collectList().block();
        filter = new AuthFilter();
        tokens = mock(TokenService.class);
        ReflectionTestUtils.setField(filter, "tokenService", tokens);
        var whites = new IgnoreWhiteProperties();
        whites.setWhites(List.of("/**"));
        ReflectionTestUtils.setField(filter, "ignoreWhite", whites);
    }

    @AfterEach
    void close() {
        context.close();
    }

    @TestFactory
    Stream<DynamicTest> publicReadsReachProductWithoutTokenOrForgedIdentity() {
        return Stream.of("/app/products", "/app/products/123", "/app/categories", "/app/comments")
            .map(path -> DynamicTest.dynamicTest(path, () -> {
                var exchange = request("GET", path, null);
                assertOwner(exchange, "product");
                var reached = new AtomicBoolean();
                filter.filter(exchange, clean -> {
                    reached.set(true);
                    for (String header : List.of(
                        SecurityConstants.HEADER_USER_ID,
                        SecurityConstants.HEADER_USER_KEY,
                        SecurityConstants.HEADER_USER_NICK_NAME,
                        SecurityConstants.HEADER_USER_AVATAR,
                        SecurityConstants.HEADER_ROLE_PERMISSION,
                        "X-Member-Id", "memberId")) {
                        assertNull(clean.getRequest().getHeaders().getFirst(header));
                    }
                    assertUnchangedRequest(exchange, clean);
                    return Mono.empty();
                }).block();
                assertTrue(reached.get());
                verifyNoInteractions(tokens);
            }));
    }

    @TestFactory
    Stream<DynamicTest> memberWritesAndPrivateReadsRequireMemberSession() {
        return Stream.of(
                new String[]{"GET", "/app/favorites", "product"},
                new String[]{"GET", "/app/favorites/count", "product"},
                new String[]{"GET", "/app/favorites/exists", "product"},
                new String[]{"POST", "/app/favorites", "product"},
                new String[]{"DELETE", "/app/favorites/123", "product"},
                new String[]{"GET", "/app/browse-historys", "product"},
                new String[]{"POST", "/app/browse-historys", "product"},
                new String[]{"DELETE", "/app/browse-historys", "product"},
                new String[]{"DELETE", "/app/browse-historys/clear", "product"},
                new String[]{"POST", "/mall/app/comments", "order"})
            .map(item -> DynamicTest.dynamicTest(item[0] + " " + item[1], () -> {
                var anonymous = request(item[0], item[1], null);
                assertOwner(anonymous, item[2]);
                filter.filter(anonymous, ignored -> {
                    fail("后台宽泛白名单不能放行会员私有接口");
                    return Mono.empty();
                }).block();
                assertTrue(anonymous.getResponse().getBodyAsString().block().contains("401"));
                var detail =
                    new SecurityUserDetail()
                        .setNickName("会员昵称")
                        .setAvatar("trusted-avatar");
                when(tokens.getSecurityUser("member-token", "member"))
                    .thenReturn(
                        new SecurityUser()
                            .setUserId(7L)
                            .setUserType("member")
                            .setDetail(detail));
                var authenticated = request(item[0], item[1], "member-token");
                var reached = new AtomicBoolean();
                filter.filter(authenticated, clean -> {
                    reached.set(true);
                    assertEquals(
                        "7",
                        clean.getRequest().getHeaders().getFirst(SecurityConstants.HEADER_USER_ID));
                    assertEquals(
                        ServletUtils.urlEncode("会员昵称"),
                        clean.getRequest().getHeaders().getFirst(SecurityConstants.HEADER_USER_NICK_NAME));
                    assertEquals(
                        "trusted-avatar",
                        clean.getRequest().getHeaders().getFirst(SecurityConstants.HEADER_USER_AVATAR));
                    assertNull(
                        clean.getRequest().getHeaders().getFirst(SecurityConstants.HEADER_USER_KEY));
                    assertNull(
                        clean.getRequest().getHeaders().getFirst(SecurityConstants.HEADER_ROLE_PERMISSION));
                    assertNull(clean.getRequest().getHeaders().getFirst("X-Member-Id"));
                    assertNull(clean.getRequest().getHeaders().getFirst("memberId"));
                    assertUnchangedRequest(authenticated, clean);
                    return Mono.empty();
                }).block();
                assertTrue(reached.get());
                verify(tokens, never()).getSecurityUser(anyString(), eq("admin"));
            }));
    }

    @Test
    void unsupportedMethodsAndSimilarPathsHaveNoCommerceRoute() {
        for (var item : new String[][]{
            {"PATCH", "/app/products"}, {"POST", "/app/comments"},
            {"GET", "/app/products/123/extra"}, {"GET", "/app/categories/123"},
            {"GET", "/application/products"}, {"GET", "/app/favorites-extra"}}) {
            assertTrue(matches(request(item[0], item[1], null)).isEmpty(), item[1]);
        }
    }

    private MockServerWebExchange request(String method, String path, String token) {
        var request =
            MockServerHttpRequest.method(
                    HttpMethod.valueOf(method), path + "?spuId=123&pageNum=2")
            .header(SecurityConstants.HEADER_USER_ID, "999")
            .header(SecurityConstants.HEADER_USER_KEY, "forged-session")
            .header(SecurityConstants.HEADER_USER_NICK_NAME, "forged-name")
            .header(SecurityConstants.HEADER_USER_AVATAR, "forged-avatar")
            .header(SecurityConstants.HEADER_ROLE_PERMISSION, "*:*:*")
            .header("X-Member-Id", "999")
            .header("memberId", "999");
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return MockServerWebExchange.from(request);
    }

    private List<Route> matches(ServerWebExchange exchange) {
        return routes.stream()
            .filter(
                route -> Boolean.TRUE.equals(
                    Mono.from(route.getPredicate().apply(exchange)).block()))
            .toList();
    }

    private void assertOwner(ServerWebExchange exchange, String owner) {
        var matched = matches(exchange);
        assertEquals(1, matched.size());
        assertEquals("lb://lemnt-" + owner, matched.get(0).getUri().toString());
        assertTrue(matched.get(0).getFilters().isEmpty(), "业务原路径不能被StripPrefix改写");
    }

    private void assertUnchangedRequest(ServerWebExchange original, ServerWebExchange forwarded) {
        assertEquals(original.getRequest().getURI(), forwarded.getRequest().getURI());
        assertEquals(original.getRequest().getMethod(), forwarded.getRequest().getMethod());
    }
}
