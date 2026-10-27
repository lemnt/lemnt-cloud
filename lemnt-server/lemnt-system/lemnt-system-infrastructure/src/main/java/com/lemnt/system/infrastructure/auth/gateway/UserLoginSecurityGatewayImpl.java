package com.lemnt.system.infrastructure.auth.gateway;

import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.system.domain.auth.gateway.UserLoginSecurityGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** UserLoginSecurity 持久化实现。 */
@Component
@RequiredArgsConstructor
public class UserLoginSecurityGatewayImpl implements UserLoginSecurityGateway {
    private final RedisStringService redisStringService;

    @Override
    public void unlockUser(String userName) {
        String cacheKey = CacheConstants.PWD_ERR_CNT_KEY + userName;
        redisStringService.delete(cacheKey);
    }
}
