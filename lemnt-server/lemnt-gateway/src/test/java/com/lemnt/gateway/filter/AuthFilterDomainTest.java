package com.lemnt.gateway.filter;

import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.properties.TokenProperties;
import com.lemnt.common.security.service.TokenService;
import com.lemnt.common.security.utils.JwtUtils;
import com.lemnt.common.snowflake.utils.SnowflakeUtils;
import com.lemnt.gateway.config.properties.IgnoreWhiteProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 使用真实 JWT 签名和网关总入口验证两个身份域互斥。
 */
class AuthFilterDomainTest {
    private AuthFilter filter;
    private JwtUtils jwt;
    private RedisStringService redis;
    private String memberToken;

    @BeforeEach
    void setup() {
        var properties = new TokenProperties();
        properties.setSecret("test-secret-only-".repeat(5));
        jwt = new JwtUtils(properties);
        redis = mock(RedisStringService.class);
        filter = new AuthFilter();
        ReflectionTestUtils.setField(filter, "ignoreWhite", new IgnoreWhiteProperties());
        memberToken = jwt.createToken(Map.of(SecurityConstants.USER_KEY, "member-session",
                SecurityConstants.DETAILS_USER_ID, "7", SecurityConstants.DETAILS_USERNAME, "member",
                SecurityConstants.USER_TYPE, SecurityConstants.MEMBER_USER_TYPE));
        var user = new SecurityUser();
        user.setUserType(SecurityConstants.MEMBER_USER_TYPE);
        user.setUserId(7L);
        user.setToken("member-session");
        user.setUsername("member");
        user.setRoles(Set.of());
        when(redis.getObj(CacheConstants.LOGIN_TOKEN_KEY + "member-session", SecurityUser.class)).thenReturn(user);
        var tokens = new TokenService(redis, mock(SnowflakeUtils.class), jwt);
        ReflectionTestUtils.setField(filter, "tokenService", tokens);
    }

    private String adminToken() {
        return jwt.createToken(Map.of(
                SecurityConstants.USER_KEY, "admin-session",
                SecurityConstants.DETAILS_USER_ID, "7",
                SecurityConstants.DETAILS_USERNAME, "admin"));
    }

    private MockServerWebExchange request(String path, String token) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path)
                .header("Authorization", "Bearer " + token));
    }

    @Test
    void memberAccessAndRefreshTokensCannotEnterAdminRoutes() {
        for (String token : List.of(memberToken, "mbr_r_" + "b".repeat(43))) {
            var exchange = request("/member/admin/users", token);
            filter.filter(exchange, ignored -> {
                fail("会员令牌不得访问后台接口"); return Mono.empty();
            }).block();
            assertTrue(exchange.getResponse().getBodyAsString().block().contains("401"));
        }
        verify(redis, never()).getObj(anyString(), eq(SecurityUser.class));
    }

    @Test
    void validSignedAdminTokenCannotEnterPrivateMemberRoutesEvenWithBroadAdminWhitelist() {
        var whites = new IgnoreWhiteProperties();
        whites.setWhites(List.of("/**"));
        ReflectionTestUtils.setField(filter, "ignoreWhite", whites);
        for (String path : List.of("/member/app/user", "/trade/app/cart", "/mall/app/private")) {
            var exchange = request(path, adminToken());
            filter.filter(exchange, ignored -> {
                fail("后台令牌不得访问会员接口"); return Mono.empty();
            }).block();
            assertTrue(exchange.getResponse().getBodyAsString().block().contains("401"));
        }
        verify(redis, never()).getObj(anyString(), eq(SecurityUser.class));
    }

    @Test
    void eachIdentityDomainStillAcceptsItsOwnValidSession() {
        var admin = new SecurityUser().setUserId(7L).setToken("admin-session")
                .setUsername("admin").setRoles(Set.of("admin"));
        when(redis.getObj(CacheConstants.LOGIN_TOKEN_KEY + "admin-session", SecurityUser.class)).thenReturn(admin);
        var adminReached = new AtomicBoolean();
        filter.filter(request("/member/admin/users", adminToken()), exchange -> {
            adminReached.set(true);
            assertEquals("7", exchange.getRequest().getHeaders().getFirst(SecurityConstants.HEADER_USER_ID));
            return Mono.empty();
        }).block();
        assertTrue(adminReached.get());
        var memberReached = new AtomicBoolean();
        filter.filter(request("/trade/app/cart", memberToken), exchange -> {
            memberReached.set(true);
            assertEquals("7", exchange.getRequest().getHeaders().getFirst(SecurityConstants.HEADER_USER_ID));
            assertNull(exchange.getRequest().getHeaders().getFirst(SecurityConstants.HEADER_USER_KEY));
            return Mono.empty();
        }).block();
        assertTrue(memberReached.get());
    }
    /** auth删除公共会话后，网关无需Dubbo或重启即可拒绝旧会员token。 */
    @Test
    void revokedMemberTokenIsRejectedAtGateway() {
        var reached = new AtomicBoolean();
        filter.filter(request("/member/app/users/user", memberToken), exchange -> {
            reached.set(true);
            return Mono.empty();
        }).block();
        assertTrue(reached.get());
        doAnswer(invocation -> {
            java.util.function.Consumer<String> action = invocation.getArgument(1);
            action.accept(CacheConstants.LOGIN_TOKEN_KEY + "member-session");
            return null;
        }).when(redis).scan(eq(CacheConstants.LOGIN_TOKEN_KEY + "*"), any());
        doAnswer(invocation -> {
            when(redis.getObj(CacheConstants.LOGIN_TOKEN_KEY + "member-session", SecurityUser.class)).thenReturn(null);
            return null;
        }).when(redis).delete(CacheConstants.LOGIN_TOKEN_KEY + "member-session");
        var tokens = (TokenService) ReflectionTestUtils.getField(filter, "tokenService");
        tokens.revokeMemberSessions(7L);
        var exchange = request("/member/app/users/user", memberToken);
        filter.filter(exchange, ignored -> {
            fail("已撤销的会员会话不得进入业务服务");
            return Mono.empty();
        }).block();
        assertTrue(exchange.getResponse().getBodyAsString().block().contains("401"));
    }

}
