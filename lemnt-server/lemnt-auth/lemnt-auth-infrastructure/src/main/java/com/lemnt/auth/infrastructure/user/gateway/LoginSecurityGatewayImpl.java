package com.lemnt.auth.infrastructure.user.gateway;

import com.lemnt.auth.domain.user.gateway.LoginSecurityGateway;
import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.redis.service.RedisStringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 登录安全网关实现（基于 Redis）
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class LoginSecurityGatewayImpl implements LoginSecurityGateway {

    private final RedisStringService redisStringService;

    /**
     * 密码错误最大重试次数
     */
    private static final int MAX_RETRY_COUNT = CacheConstants.PASSWORD_MAX_RETRY_COUNT;

    /**
     * 锁定时间（分钟）
     */
    private static final long LOCK_TIME = CacheConstants.PASSWORD_LOCK_TIME;

    @Override
    public int getRetryCount(String username) {
        Integer count = redisStringService.getObj(getCacheKey(username), Integer.class);
        return count != null ? count : 0;
    }

    @Override
    public void incrementRetryCount(String username) {
        int count = getRetryCount(username) + 1;
        redisStringService.setObj(getCacheKey(username), count, LOCK_TIME, TimeUnit.MINUTES);
    }

    @Override
    public void clearRetryCount(String username) {
        String key = getCacheKey(username);
        if (redisStringService.hasKey(key)) {
            redisStringService.delete(key);
        }
    }

    @Override
    public boolean isLocked(String username) {
        int retryCount = getRetryCount(username);
        return retryCount >= MAX_RETRY_COUNT;
    }

    @Override
    public String getBlackIpList() {
        Object value = redisStringService.get(CacheConstants.SYS_LOGIN_BLACKIPLIST);
        return value != null ? String.valueOf(value) : null;
    }

    private String getCacheKey(String username) {
        return CacheConstants.PWD_ERR_CNT_KEY + username;
    }
}