package com.lemnt.gateway.filter;

import com.lemnt.common.security.constant.SecurityConstants;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

class MemberRequestSecurityTest {
    static final String ACCESS = "mbr_a_" + "a".repeat(43);

    MockServerWebExchange exchange(HttpMethod method, String path, String token) {
        var request =
                MockServerHttpRequest.method(method, path)
                        .header(SecurityConstants.HEADER_USER_ID, "999")
                        .header(SecurityConstants.HEADER_ROLE_PERMISSION, "*:*:*")
                        .header("X-Member-Id", "999")
                        .header("memberId", "999");
        if (token != null) request.header("Authorization", "Bearer " + token);
        return MockServerWebExchange.from(request);
    }

    @Test
    void publicRulesAreExactAndStripSpoofedIdentity() {
        var filter =
                new MemberRequestSecurity(
                        token -> {
                            fail("public route must not call authority");
                            return null;
                        });
        for (var route :
                new String[][] {
                    {"POST", "/member/app/auth/sms-login"},
                    {"PUT", "/member/app/user/reset-password"},
                    {"GET", "/member/app/auth/social-auth-redirect"}
                }) {
            var ex = exchange(HttpMethod.valueOf(route[0]), route[1], null);
            var called = new AtomicBoolean();
            filter.filter(
                            ex,
                            route[1],
                            clean -> {
                                called.set(true);
                                assertNull(
                                        clean.getRequest()
                                                .getHeaders()
                                                .getFirst(SecurityConstants.HEADER_USER_ID));
                                assertNull(
                                        clean.getRequest()
                                                .getHeaders()
                                                .getFirst(
                                                        SecurityConstants.HEADER_ROLE_PERMISSION));
                                assertNull(clean.getRequest().getHeaders().getFirst("X-Member-Id"));
                                assertNull(clean.getRequest().getHeaders().getFirst("memberId"));
                                return Mono.empty();
                            })
                    .block();
            assertTrue(called.get());
        }
        for (var route :
                new String[][] {
                    {"GET", "/member/app/auth/sms-login"},
                    {"POST", "/member/app/user/reset-password"},
                    {"POST", "/member/app/auth/login/extra"},
                    {"GET", "/member/app/user"}
                }) {
            var ex = exchange(HttpMethod.valueOf(route[0]), route[1], null);
            filter.filter(
                            ex,
                            route[1],
                            clean -> {
                                fail("unlisted public route");
                                return Mono.empty();
                            })
                    .block();
            assertTrue(ex.getResponse().getBodyAsString().block().contains("401"));
        }
    }

    @Test
    void adminRefreshAndMissingTokensCannotReachAuthority() {
        var filter =
                new MemberRequestSecurity(
                        token -> {
                            fail("wrong token kind");
                            return null;
                        });
        for (String token : new String[] {null, "eyJ.admin.jwt", "mbr_r_" + "a".repeat(43)}) {
            var ex = exchange(HttpMethod.GET, "/trade/app/cart", token);
            filter.filter(
                            ex,
                            "/trade/app/cart",
                            clean -> {
                                fail("private route");
                                return Mono.empty();
                            })
                    .block();
            assertTrue(ex.getResponse().getBodyAsString().block().contains("401"));
        }
    }

    @Test
    void validTokenUsesAuthoritativeLookupOnWorkerAndPreservesToken() {
        var called = new AtomicBoolean();
        var filter =
                new MemberRequestSecurity(
                        token -> {
                            assertEquals(ACCESS, token);
                            assertTrue(Thread.currentThread().getName().contains("boundedElastic"));
                            return 123L;
                        });
        var ex = exchange(HttpMethod.GET, "/member/app/user", ACCESS);
        filter.filter(
                        ex,
                        "/member/app/user",
                        clean -> {
                            called.set(true);
                            assertEquals(
                                    "Bearer " + ACCESS,
                                    clean.getRequest().getHeaders().getFirst("Authorization"));
                            return Mono.empty();
                        })
                .block();
        assertTrue(called.get());
    }

    @Test
    void unavailableAuthorityFailsClosedAndDownstreamFailureIsNotMasked() {
        var ex = exchange(HttpMethod.GET, "/member/app/user", ACCESS);
        new MemberRequestSecurity(
                        token -> {
                            throw new IllegalStateException("unavailable");
                        })
                .filter(
                        ex,
                        "/member/app/user",
                        clean -> {
                            fail("must fail closed");
                            return Mono.empty();
                        })
                .block();
        assertTrue(ex.getResponse().getBodyAsString().block().contains("503"));
        var downstream = new IllegalArgumentException("downstream");
        assertSame(
                downstream,
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new MemberRequestSecurity(token -> 123L)
                                        .filter(
                                                exchange(
                                                        HttpMethod.GET, "/member/app/user", ACCESS),
                                                "/member/app/user",
                                                clean -> Mono.error(downstream))
                                        .block()));
    }

    @Test
    void boundaryDoesNotCaptureSimilarPrefixes() {
        assertTrue(MemberRequestSecurity.memberPath("/member/app/user"));
        assertTrue(MemberRequestSecurity.memberPath("/trade/app/cart"));
        assertFalse(MemberRequestSecurity.memberPath("/member/application"));
        assertFalse(MemberRequestSecurity.memberPath("/trade/admin/orders"));
    }
}
