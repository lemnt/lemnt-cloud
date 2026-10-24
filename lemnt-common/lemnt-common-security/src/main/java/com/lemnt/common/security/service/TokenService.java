package com.lemnt.common.security.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.common.snowflake.utils.SnowflakeUtils;
import jakarta.servlet.http.HttpServletRequest;

import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.core.utils.ServletUtils;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.core.utils.ip.IpUtils;
import com.lemnt.common.security.utils.SecurityUtils;

/**
 * token验证处理
 *
 * @author wangxx
 */
@Component
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    private final RedisStringService redisStringService;
    private final SnowflakeUtils snowflakeUtils;
    private final JwtUtils jwtUtils;

    public TokenService(RedisStringService redisStringService, SnowflakeUtils snowflakeUtils, JwtUtils jwtUtils) {
        this.redisStringService = redisStringService;
        this.snowflakeUtils = snowflakeUtils;
        this.jwtUtils = jwtUtils;
    }

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private final static long TOKEN_EXPIRE_TIME = CacheConstants.EXPIRATION;

    private final static Long TOKEN_REFRESH_THRESHOLD_MINUTES = CacheConstants.REFRESH_TIME * MILLIS_MINUTE;

    /**
     * 创建令牌
     */
    public Map<String, Object> createToken(SecurityUser securityUser) {
        String token = snowflakeUtils.generateStr();
        Long userId = securityUser.getDetail().getUserId();
        String userName = securityUser.getDetail().getUserName();
        securityUser.setToken(token);
        securityUser.setUserId(userId);
        securityUser.setUsername(userName);
        securityUser.setIpaddr(IpUtils.getIpAddr());
        refreshToken(securityUser);

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put(SecurityConstants.USER_KEY, token);
        claimsMap.put(SecurityConstants.DETAILS_USER_ID, userId);
        claimsMap.put(SecurityConstants.DETAILS_USERNAME, userName);
        claimsMap.put(SecurityConstants.ROLE_KEY, String.join(",", securityUser.getRoles()));

        Map<String, Object> rspMap = new HashMap<>();
        rspMap.put("access_token", jwtUtils.createToken(claimsMap));
        rspMap.put("expires_in", TOKEN_EXPIRE_TIME);
        return rspMap;
    }

    /**
     * 获取用户身份信息
     */
    public SecurityUser getSecurityUser() {
        return getSecurityUser(ServletUtils.getRequest());
    }

    /**
     * 获取用户身份信息
     */
    public SecurityUser getSecurityUser(HttpServletRequest request) {
        String token = SecurityUtils.getToken(request);
        return getSecurityUser(token);
    }

    /**
     * 获取用户身份信息
     */
    public SecurityUser getSecurityUser(String token) {
        SecurityUser user = null;
        try {
            if (StringUtils.isNotBlank(token)) {
                String userKey = jwtUtils.getUserKey(token);
                user = redisStringService.getObj(getTokenKey(userKey), SecurityUser.class);
                return user;
            }
        } catch (Exception e) {
            log.error("获取用户信息异常'{}'", e.getMessage());
        }
        return user;
    }

    /**
     * 获取用户身份信息
     */
    public SecurityUser getSecurityUserByUserKey(String userKey) {
        SecurityUser user = null;
        try {
            if (StringUtils.isNotBlank(userKey)) {
                user = redisStringService.getObj(getTokenKey(userKey), SecurityUser.class);
                return user;
            }
        } catch (Exception e) {
            log.error("获取用户信息异常'{}'", e.getMessage());
        }
        return user;
    }

    /**
     * 设置用户身份信息
     */
    public void setSecurityUser(SecurityUser securityUser) {
        if (Objects.nonNull(securityUser) && StringUtils.isNotBlank(securityUser.getToken())) {
            refreshToken(securityUser);
        }
    }

    /**
     * 删除用户缓存信息
     */
    public void delSecurityUser(String token) {
        if (StringUtils.isNotBlank(token)) {
            try {
                String userkey = jwtUtils.getUserKey(token);
                redisStringService.delete(getTokenKey(userkey));
            } catch (Exception e) {
                log.warn("删除用户登录态时令牌无效：{}", e.getMessage());
            }
        }
    }

    /**
     * 验证令牌有效期，相差不足120分钟，自动刷新缓存
     */
    public void verifySecurityUser(SecurityUser securityUser) {
        long expireTime = securityUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= TOKEN_REFRESH_THRESHOLD_MINUTES) {
            refreshToken(securityUser);
        }
    }

    /**
     * 刷新令牌有效期
     */
    public void refreshToken(SecurityUser securityUser) {
        securityUser.setLoginTime(System.currentTimeMillis());
        securityUser.setExpireTime(securityUser.getLoginTime() + TOKEN_EXPIRE_TIME * MILLIS_MINUTE);
        String userKey = getTokenKey(securityUser.getToken());
        redisStringService.setObj(userKey, securityUser, TOKEN_EXPIRE_TIME, TimeUnit.MINUTES);
    }

    private String getTokenKey(String token) {
        return CacheConstants.LOGIN_TOKEN_KEY + token;
    }
}
