package com.lemnt.auth.start;

import com.lemnt.auth.application.member.service.MemberAuthenticationService;
import com.lemnt.auth.domain.member.gateway.MemberAccountGateway;
import com.lemnt.auth.domain.member.model.MemberLoginInfo;
import com.lemnt.auth.domain.member.model.MemberToken;
import com.lemnt.auth.infrastructure.member.gateway.MemberTokenGatewayImpl;
import com.lemnt.auth.infrastructure.user.gateway.PasswordEncoderGatewayImpl;
import com.lemnt.auth.infrastructure.user.gateway.LoginSecurityGatewayImpl;
import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.properties.TokenProperties;
import com.lemnt.common.security.service.TokenService;
import com.lemnt.common.security.utils.JwtUtils;
import com.lemnt.common.snowflake.utils.SnowflakeUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/** 使用 RPC 边界和真实公共密码/令牌组件，认证中心测试不启动数据库。 */
class MemberAuthenticationIntegrationTest {
    MemberAuthenticationService auth;
    MemberAccountGateway accounts;
    TokenService tokenService;
    JwtUtils jwtUtils;
    final Map<String, SecurityUser> cachedSessions = new ConcurrentHashMap<>();
    final Map<String, Integer> retryCounts = new ConcurrentHashMap<>();
    final AtomicInteger status = new AtomicInteger(1);
    final AtomicReference<String> passwordHash = new AtomicReference<>();
    final Long id = 1L;

    @BeforeEach
    void setup() {
        var redis = Mockito.mock(RedisStringService.class);
        var snowflake = Mockito.mock(SnowflakeUtils.class);
        var properties = new TokenProperties();
        properties.setSecret("test-secret-only-".repeat(5));
        jwtUtils = new JwtUtils(properties);
        Mockito.when(snowflake.generateStr()).thenAnswer(invocation -> UUID.randomUUID().toString());
        Mockito.when(redis.getObj(ArgumentMatchers.anyString(), ArgumentMatchers.eq(SecurityUser.class)))
                .thenAnswer(invocation -> cachedSessions.get(invocation.getArgument(0)));
        Mockito.when(redis.getObj(ArgumentMatchers.anyString(), ArgumentMatchers.eq(Integer.class)))
                .thenAnswer(invocation -> retryCounts.get(invocation.getArgument(0)));
        Mockito.when(redis.hasKey(ArgumentMatchers.anyString())).thenAnswer(invocation ->
                retryCounts.containsKey(invocation.getArgument(0)) || cachedSessions.containsKey(invocation.getArgument(0)));
        Mockito.doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            if (value instanceof SecurityUser user) { cachedSessions.put(key, user); }
            else { retryCounts.put(key, (Integer) value); }
            return null;
        }).when(redis).setObj(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
                ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class));
        Mockito.doAnswer(invocation -> {
            cachedSessions.remove(invocation.getArgument(0));
            retryCounts.remove(invocation.getArgument(0));
            return null;
        }).when(redis).delete(ArgumentMatchers.anyString());
        Mockito.doAnswer(invocation -> {
            java.util.function.Consumer<String> action = invocation.getArgument(1);
            java.util.List.copyOf(cachedSessions.keySet()).forEach(action);
            return null;
        }).when(redis).scan(ArgumentMatchers.eq(CacheConstants.LOGIN_TOKEN_KEY + "*"), ArgumentMatchers.any());
        Mockito.when(redis.setObjIfPresent(ArgumentMatchers.anyString(), ArgumentMatchers.any(),
                ArgumentMatchers.anyLong(), ArgumentMatchers.any(TimeUnit.class))).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            SecurityUser value = invocation.getArgument(1);
            return cachedSessions.replace(key, value) != null;
        });
        accounts = Mockito.mock(MemberAccountGateway.class);
        Mockito.when(accounts.findByMobile("13800000001")).thenAnswer(invocation ->
                new MemberLoginInfo(id, "13800000001", status.get(), passwordHash.get()));
        Mockito.when(accounts.findStatusByMemberId(id)).thenAnswer(invocation -> status.get());
        tokenService = new TokenService(redis, snowflake, jwtUtils);
        auth = new MemberAuthenticationService(accounts, new PasswordEncoderGatewayImpl(),
                new LoginSecurityGatewayImpl(redis), new MemberTokenGatewayImpl(tokenService));
    }

    /** 模拟会员 RPC 读取到密码更新后的登录资料。 */
    private void setPassword(Long memberId, String password) {
        assertEquals(id, memberId);
        passwordHash.set(new PasswordEncoderGatewayImpl().encode(password));
    }

    private MemberToken login() {
        setPassword(id, "Member-password-2026");
        return auth.login("13800000001", "Member-password-2026");
    }

    @Test
    void passwordLoginUsesBackendJwtAndSharedLoginKey() {
        var tokens = login();

        var claims = jwtUtils.parseToken(tokens.accessToken());

        assertEquals("member", jwtUtils.getUserType(claims));

        assertEquals(id, auth.authenticate(tokens.accessToken()));

        assertNull(tokenService.getSecurityUser(tokens.accessToken()));

        assertEquals(2592000L, tokens.expiresIn());

        assertEquals(1, cachedSessions.size());

        assertTrue(cachedSessions.keySet().iterator().next().startsWith(
                CacheConstants.LOGIN_TOKEN_KEY));

        assertNull(auth.authenticate("eyJ.admin.jwt"));

    }

    @Test
    void refreshRenewsExistingSessionUsingSameAccessToken() {
        var first = login();

        var next = auth.refresh(first.accessToken());

        assertEquals(first.accessToken(), next.accessToken());

        assertEquals(id, auth.authenticate(next.accessToken()));

        assertEquals(2592000L, next.expiresIn());

    }

    @Test
    void concurrentRenewalKeepsOneSessionAndDoesNotRotateJwt() throws Exception {
        var tokens = login();

        var pool = Executors.newFixedThreadPool(2);

        try {
            var first = pool.submit(() -> auth.refresh(tokens.accessToken()));

            var second = pool.submit(() -> auth.refresh(tokens.accessToken()));

            assertEquals(tokens.accessToken(), first.get(15, TimeUnit.SECONDS).accessToken());

            assertEquals(tokens.accessToken(), second.get(15, TimeUnit.SECONDS).accessToken());

            assertEquals(1, cachedSessions.size());

        } finally {
            pool.shutdownNow();

        }
    }

    @Test
    void logoutOnlyRevokesCurrentSessionAndReadsUpdatedCredentials() {
        var first = login();

        var second = auth.login("13800000001", "Member-password-2026");

        assertTrue(auth.logout("Bearer " + first.accessToken()));

        assertTrue(auth.logout("Bearer " + first.accessToken()));

        assertNull(auth.authenticate(first.accessToken()));

        assertEquals(id, auth.authenticate(second.accessToken()));

        setPassword(id, "New-password-2026");

        assertEquals(id, auth.authenticate(second.accessToken()));

        assertThrows(
                SecurityException.class, () -> auth.login("13800000001", "Member-password-2026"));

        assertNotNull(auth.login("13800000001", "New-password-2026"));

    }

    @Test
    void disabledMemberSessionsCannotReturnAfterReenable() throws Exception {
        var tokens = login();

        status.set(0);
        new com.lemnt.auth.application.dubbo.MemberSessionClientImpl(auth).revokeMemberSessions(id);

        assertNull(auth.authenticate(tokens.accessToken()));

        assertThrows(SecurityException.class, () -> auth.refresh(tokens.accessToken()));

        status.set(1);

        assertNull(auth.authenticate(tokens.accessToken()));
        assertNotNull(login());

    }

    @Test
    void repeatedFailedLoginUsesBackendRedisRetryPolicy() throws Exception {
        setPassword(id, "Member-password-2026");

        for (int n = 0; n < 5; n++) {
            assertThrows(
                    SecurityException.class, () -> auth.login("13800000001", "wrong-password"));

        }
        assertThrows(
                SecurityException.class, () -> auth.login("13800000001", "Member-password-2026"));

        assertEquals(0, cachedSessions.size());

        retryCounts.clear();

        assertNotNull(auth.login("13800000001", "Member-password-2026"));

    }

    @Test
    void successfulLoginAuditsOnceButRefreshAndWrongPasswordDoNot() {
        var token=login();
        auth.refresh(token.accessToken());
        assertThrows(SecurityException.class,()->auth.login("13800000001","wrong"));
        Mockito.verify(accounts,Mockito.times(1)).recordLogin(id);
    }
    @Test
    void auditFailureRevokesNewSharedSessionBeforeReturningFailure() {
        Mockito.doThrow(new IllegalStateException("审计RPC失败")).when(accounts).recordLogin(id);
        assertThrows(IllegalStateException.class,()->login());
        assertTrue(cachedSessions.isEmpty());
    }
    @Test
    void failedTokenCreationDoesNotWriteLoginAudit() {
        var tokens=Mockito.mock(com.lemnt.auth.domain.member.gateway.MemberTokenGateway.class);
        Mockito.when(tokens.create(id,"13800000001")).thenThrow(new IllegalStateException("会话写入失败"));
        var security=Mockito.mock(com.lemnt.auth.domain.user.gateway.LoginSecurityGateway.class);
        setPassword(id,"Member-password-2026");
        var service=new MemberAuthenticationService(accounts,new PasswordEncoderGatewayImpl(),security,tokens);
        assertThrows(IllegalStateException.class,()->service.login("13800000001","Member-password-2026"));
        Mockito.verify(accounts,Mockito.never()).recordLogin(id);
    }

    @Test
    void successfulLoginPassesObservedIpOnlyAfterTokenCreation() {
        setPassword(id,"Member-password-2026");
        var token=auth.login("13800000001","Member-password-2026","203.0.113.7");
        assertEquals(id,auth.authenticate(token.accessToken()));
        Mockito.verify(accounts).recordLogin(id,"203.0.113.7");
    }

    /** 多终端撤销后网关读取不到会话，同ID管理员及其他会员不受影响。 */
    @Test
    void revocationRemovesAllMemberSessionsWithoutAffectingOtherIdentityDomains() {
        var first = login();
        var second = login();
        var admin = new SecurityUser().setUserId(id).setUserType("admin").setToken("admin");
        var other = new SecurityUser().setUserId(id + 1).setUserType("member").setToken("other");
        cachedSessions.put(CacheConstants.LOGIN_TOKEN_KEY + "admin", admin);
        cachedSessions.put(CacheConstants.LOGIN_TOKEN_KEY + "other", other);
        new com.lemnt.auth.application.dubbo.MemberSessionClientImpl(auth).revokeMemberSessions(id);
        assertNull(tokenService.getSecurityUser(first.accessToken(), "member"));
        assertNull(tokenService.getSecurityUser(second.accessToken(), "member"));
        assertEquals(2, cachedSessions.size());
        assertSame(admin, cachedSessions.get(CacheConstants.LOGIN_TOKEN_KEY + "admin"));
        assertSame(other, cachedSessions.get(CacheConstants.LOGIN_TOKEN_KEY + "other"));
        auth.revokeMemberSessions(id);
        assertEquals(2, cachedSessions.size());
    }

    /** 模拟刷新已经读到旧对象、随后撤销完成，SET XX不得恢复旧会话。 */
    @Test
    void staleRefreshCannotResurrectRevokedSession() {
        var token = login();
        var stale = tokenService.getSecurityUser(token.accessToken(), "member");
        auth.revokeMemberSessions(id);
        assertThrows(SecurityException.class, () -> tokenService.refreshExistingToken(stale));
        assertNull(tokenService.getSecurityUser(token.accessToken(), "member"));
        assertTrue(cachedSessions.isEmpty());
    }

}
