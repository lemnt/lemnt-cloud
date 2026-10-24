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
