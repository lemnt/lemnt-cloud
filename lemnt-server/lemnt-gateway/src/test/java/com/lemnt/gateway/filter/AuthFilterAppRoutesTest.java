package com.lemnt.gateway.filter;

import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.service.TokenService;
import com.lemnt.gateway.config.properties.IgnoreWhiteProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;

/** 公开规则、身份域与身份头清理都通过统一 AuthFilter 入口验证。 */
class AuthFilterAppRoutesTest {

    private AuthFilter filter;
    private TokenService tokenService;

    @BeforeEach
    void setup() {
        filter = new AuthFilter();
        tokenService = mock(TokenService.class);
        ReflectionTestUtils.setField(filter, "tokenService", tokenService);
        ReflectionTestUtils.setField(filter, "ignoreWhite", new IgnoreWhiteProperties());
    }

    private MockServerWebExchange exchange(String method, String path, String token) {
        var request = MockServerHttpRequest.method(HttpMethod.valueOf(method), path)
                .header(SecurityConstants.HEADER_USER_ID, "999")
                .header(SecurityConstants.HEADER_ROLE_PERMISSION, "*:*:*")
                .header("X-Member-Id", "999").header("memberId", "999");
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return MockServerWebExchange.from(request);
    }

    @Test
    void publicRulesAreExactAndStripSpoofedIdentity() {
        for (var route : new String[][] {
                {"POST", "/member/app/auth/login"},
                {"POST", "/member/app/auth/refresh-token"},
                {"POST", "/member/app/auth/logout"},
                {"GET", "/mall/app/comments"},
                {"GET", "/mall/app/categories"},
                {"GET", "/mall/app/products"},
                {"GET", "/mall/app/products/1"}}) {
            var reached = new AtomicBoolean();
            filter.filter(exchange(route[0], route[1], null), clean -> {
                reached.set(true);
                for (String header : Set.of(SecurityConstants.HEADER_USER_ID,
                        SecurityConstants.HEADER_ROLE_PERMISSION, "X-Member-Id", "memberId")) {
                    assertNull(clean.getRequest().getHeaders().getFirst(header));
                }
                return Mono.empty();
            }).block();
            assertTrue(reached.get());
        }
        verifyNoInteractions(tokenService);
    }

    @Test
    void unlistedMethodsAndSimilarPathsRequireAuthentication() {
        for (var route : new String[][] {
                {"POST", "/member/app/auth/sms-login"},
                {"PUT", "/member/app/user/reset-password"},
                {"GET", "/member/app/auth/social-auth-redirect"},
                {"GET", "/member/app/auth/sms-login"},
                {"POST", "/member/app/user/reset-password"},
                {"POST", "/member/app/auth/login/extra"},
                {"GET", "/member/app/user"},
                {"POST", "/files/app/upload"},
                {"POST", "/mall/app/comments"},
                {"GET", "/mall/app/comments/1"},
                {"GET", "/mall/app/products/0"},
                {"GET", "/mall/app/products/1/edit"},
                {"POST", "/mall/app/products"}}) {
            var current = exchange(route[0], route[1], null);
            filter.filter(current, ignored -> {
                fail("私有接口不得匿名进入");
                return Mono.empty();
            }).block();
            assertTrue(current.getResponse().getBodyAsString().block().contains("401"));
        }
    }

    @Test
    void sameFilterSelectsExpectedRealmByServerPath() {
        var member = new SecurityUser().setUserId(7L).setUserType("member");
        var admin = new SecurityUser().setUserId(8L).setToken("admin-session")
                .setUsername("admin").setRoles(Set.of("admin"));
        when(tokenService.getSecurityUser("token", "member")).thenReturn(member);
        when(tokenService.getSecurityUser("token", "admin")).thenReturn(admin);
        for (String path : Set.of("/files/app/upload", "/member/app/user", "/trade/app/cart", "/mall/app/private")) {
            filter.filter(exchange("GET", path, "token"), clean -> {
                assertEquals("7", clean.getRequest().getHeaders().getFirst(SecurityConstants.HEADER_USER_ID));
                assertNull(clean.getRequest().getHeaders().getFirst(SecurityConstants.HEADER_USER_KEY));
                assertNull(clean.getRequest().getHeaders().getFirst(SecurityConstants.HEADER_ROLE_PERMISSION));
                assertEquals("Bearer token", clean.getRequest().getHeaders().getFirst("Authorization"));
                return Mono.empty();
            }).block();
        }
        filter.filter(exchange("GET", "/member/application", "token"), clean -> {
            assertEquals("8", clean.getRequest().getHeaders().getFirst(SecurityConstants.HEADER_USER_ID));
            return Mono.empty();
        }).block();
        verify(tokenService, times(4)).getSecurityUser("token", "member");
        verify(tokenService).getSecurityUser("token", "admin");
    }

    @Test
    void missingCacheSessionFailsClosedAndDownstreamErrorsRemainVisible() {
        var current = exchange("GET", "/trade/app/cart", "expired");
        filter.filter(current, ignored -> {
            fail("失效会话不得进入业务服务");
            return Mono.empty();
        }).block();
        assertTrue(current.getResponse().getBodyAsString().block().contains("401"));
        when(tokenService.getSecurityUser("valid", "member"))
                .thenReturn(new SecurityUser().setUserId(7L).setUserType("member"));
        var failure = new IllegalStateException("downstream");
        assertSame(failure, assertThrows(IllegalStateException.class, () -> filter.filter(
                exchange("GET", "/trade/app/cart", "valid"), ignored -> Mono.error(failure)).block()));
    }

    @Test
    void publicMatchingUsesOriginalPathAfterStripPrefix() {
        var current = exchange("POST", "/app/auth/login", null);
        current.getAttributes().put(GATEWAY_ORIGINAL_REQUEST_URL_ATTR,
                Set.of(URI.create("http://localhost/member/app/auth/login")));
        var reached = new AtomicBoolean();
        filter.filter(current, ignored -> {
            reached.set(true);
            return Mono.empty();
        }).block();
        assertTrue(reached.get());
        verifyNoInteractions(tokenService);
    }
    @Test
    void loginAuditHeaderUsesObservedPeerAndDiscardsSpoofedHeaders() {
        var request=MockServerHttpRequest.post("/member/app/auth/login")
                .remoteAddress(new java.net.InetSocketAddress("203.0.113.7",1234))
                .header("X-Lemnt-Login-Ip","198.51.100.99").header("X-Forwarded-For","198.51.100.99").build();
        filter.filter(MockServerWebExchange.from(request),clean->{
            assertEquals("203.0.113.7",clean.getRequest().getHeaders().getFirst("X-Lemnt-Login-Ip"));
            return Mono.empty();
        }).block();
        var unknown=MockServerHttpRequest.post("/member/app/auth/login").header("X-Lemnt-Login-Ip","198.51.100.99").build();
        filter.filter(MockServerWebExchange.from(unknown),clean->{
            assertNull(clean.getRequest().getHeaders().getFirst("X-Lemnt-Login-Ip"));return Mono.empty();
        }).block();
    }

}
