package com.lemnt.common.security.service;

import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.model.SecurityUserDetail;
import com.lemnt.common.security.properties.TokenProperties;
import com.lemnt.common.security.utils.JwtUtils;
import com.lemnt.common.snowflake.utils.SnowflakeUtils;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/** 验证令牌响应、Redis TTL 与会话时间戳使用同一有效期。 */
class TokenServiceTest {
    private static final long SESSION_SECONDS = 2592000L;
    private final RedisStringService redis = mock(RedisStringService.class, withSettings().mockMaker("mock-maker-subclass"));
    private final SnowflakeUtils snowflake = mock(SnowflakeUtils.class, withSettings().mockMaker("mock-maker-subclass"));
    private final JwtUtils jwt = jwtUtils();
    private final TokenService service = new TokenService(redis, snowflake, jwt);

    @Test
    void creationShouldReturnSessionLifetimeInSeconds() {
        when(snowflake.generateStr()).thenReturn("session-1");
        SecurityUser user = user();

        Map<String, Object> result = service.createToken(user);

        assertThat(result.get("expires_in")).isEqualTo(SESSION_SECONDS);
        assertThat(user.getExpireTime() - user.getLoginTime()).isEqualTo(TimeUnit.SECONDS.toMillis(SESSION_SECONDS));
        verify(redis).setObj(CacheConstants.LOGIN_TOKEN_KEY + "session-1", user, SESSION_SECONDS, TimeUnit.SECONDS);
        // JWT 无固定 exp；滑动会话由 Redis TTL 控制。
        assertThat(jwt.parseToken((String) result.get("access_token")).getExpiration()).isNull();
    }

    @Test
    void explicitRefreshShouldUseSameLifetimeAndUnit() {
        SecurityUser user = user().setLoginTime(1L).setExpireTime(2L);
        long before = System.currentTimeMillis();

        service.refreshToken(user);

        assertThat(user.getLoginTime()).isBetween(before, System.currentTimeMillis());
        assertThat(user.getExpireTime() - user.getLoginTime()).isEqualTo(TimeUnit.SECONDS.toMillis(SESSION_SECONDS));
        verify(redis).setObj(CacheConstants.LOGIN_TOKEN_KEY + "session-1", user, SESSION_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    void remainingTimeAtRefreshThresholdShouldRenewSession() {
        SecurityUser user = user().setExpireTime(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(120));

        service.verifySecurityUser(user);

        verify(redis).setObj(CacheConstants.LOGIN_TOKEN_KEY + "session-1", user, SESSION_SECONDS, TimeUnit.SECONDS);
    }

    @Test
    void remainingTimeAboveThresholdShouldNotRenewSession() {
        SecurityUser user = user().setExpireTime(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(121));
        Long expiry = user.getExpireTime();

        service.verifySecurityUser(user);

        verifyNoInteractions(redis);
        assertThat(user.getExpireTime()).isEqualTo(expiry);
    }

    @Test
    void missingRedisSessionShouldNotAuthenticateOrRenew() {
        String token = jwt.createToken(Map.of("user_key", "session-1"));

        assertThat(service.getSecurityUser(token)).isNull();
        assertThat(service.getSecurityUserByUserKey("session-1")).isNull();
        verify(redis, never()).setObj(anyString(), any(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void memberJwtReusesLoginKeyAndCannotAuthenticateAsAdmin() {
        when(snowflake.generateStr()).thenReturn("member-session");
        SecurityUser user = user().setUserType("member");
        var result = service.createToken(user);
        String token = (String) result.get("access_token");
        assertThat(jwt.getUserType(jwt.parseToken(token))).isEqualTo("member");
        verify(redis).setObj(CacheConstants.LOGIN_TOKEN_KEY + "member-session", user,
                SESSION_SECONDS, TimeUnit.SECONDS);
        when(redis.getObj(CacheConstants.LOGIN_TOKEN_KEY + "member-session", SecurityUser.class)).thenReturn(user);
        assertThat(service.getSecurityUser(token)).isNull();
        assertThat(service.getSecurityUser(token, "member")).isSameAs(user);
        verify(redis).getObj(CacheConstants.LOGIN_TOKEN_KEY + "member-session", SecurityUser.class);
        assertThat(service.getSecurityUserByUserKey("member-session")).isNull();
    }

    @Test
    void legacyAdminJwtRemainsValidOnlyInAdminRealm() {
        String token = jwt.createToken(Map.of("user_key", "session-1"));
        SecurityUser user = user();
        when(redis.getObj(CacheConstants.LOGIN_TOKEN_KEY + "session-1", SecurityUser.class)).thenReturn(user);
        assertThat(service.getSecurityUser(token)).isSameAs(user);
        assertThat(service.getSecurityUser(token, "member")).isNull();
        verify(redis).getObj(CacheConstants.LOGIN_TOKEN_KEY + "session-1", SecurityUser.class);
    }

    @Test
    void sharedCacheKeyStillRequiresMatchingCachedIdentityType() {
        String token = jwt.createToken(Map.of("user_key", "shared-session", "user_type", "admin"));
        when(redis.getObj(CacheConstants.LOGIN_TOKEN_KEY + "shared-session", SecurityUser.class))
                .thenReturn(user().setUserType("member"));
        assertThat(service.getSecurityUser(token)).isNull();
        assertThat(service.getSecurityUserByUserKey("shared-session")).isNull();
    }

    @Test
    void logoutDeletesOnlyTheCurrentSession() {
        String token = jwt.createToken(Map.of("user_key", "member-session", "user_type", "member"));
        service.delSecurityUser(token, "member");
        verify(redis).delete(CacheConstants.LOGIN_TOKEN_KEY + "member-session");
        verify(redis, never()).delete(CacheConstants.LOGIN_TOKEN_KEY + "other-session");
    }

    /** 续期必须使用原子存在性更新，已删除会话不得重新创建。 */
    @Test
    void refreshExistingSessionUsesConditionalWriteAndRejectsMissingKey() {
        SecurityUser user = user();
        org.junit.jupiter.api.Assertions.assertThrows(SecurityException.class, () -> service.refreshExistingToken(user));
        when(redis.setObjIfPresent(CacheConstants.LOGIN_TOKEN_KEY + "session-1", user, SESSION_SECONDS, TimeUnit.SECONDS))
                .thenReturn(true);
        service.refreshExistingToken(user);
        verify(redis, times(2)).setObjIfPresent(CacheConstants.LOGIN_TOKEN_KEY + "session-1", user, SESSION_SECONDS, TimeUnit.SECONDS);
        verify(redis, never()).setObj(anyString(), any(), anyLong(), any());
    }

    /** 非法会员编号不会触发遍历公共会话。 */
    @Test
    void invalidMemberIdDoesNotScanSessionCache() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.revokeMemberSessions(null));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.revokeMemberSessions(0L));
        verifyNoInteractions(redis);
    }

    private SecurityUser user() {
        return new SecurityUser().setToken("session-1").setRoles(Set.of("user"))
                .setDetail(new SecurityUserDetail().setUserId(100L).setUserName("tester"));
    }

    private static JwtUtils jwtUtils() {
        TokenProperties properties = new TokenProperties();
        properties.setSecret("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-!");
        return new JwtUtils(properties);
    }
}
