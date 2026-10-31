package com.lemnt.common.security.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.lemnt.common.redis.service.RedisStringService;
import io.jsonwebtoken.JwtException;
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

    public TokenService(
            RedisStringService redisStringService,
            SnowflakeUtils snowflakeUtils,
            JwtUtils jwtUtils) {
        this.redisStringService = redisStringService;
        this.snowflakeUtils = snowflakeUtils;
        this.jwtUtils = jwtUtils;
    }

    /**
     * 创建令牌，expires_in 为本次签发的会话有效秒数；后续续期由 Redis 会话控制。
     */
    public Map<String, Object> createToken(SecurityUser securityUser) {
        if (securityUser.getUserType() == null) {
            securityUser.setUserType(SecurityConstants.ADMIN_USER_TYPE);
        }
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
        claimsMap.put(SecurityConstants.USER_TYPE, securityUser.getUserType());
        claimsMap.put(SecurityConstants.DETAILS_USER_ID, userId);
        claimsMap.put(SecurityConstants.DETAILS_USERNAME, userName);
        claimsMap.put(SecurityConstants.ROLE_KEY, String.join(",", securityUser.getRoles()));

        Map<String, Object> rspMap = new HashMap<>();
        rspMap.put("access_token", jwtUtils.createToken(claimsMap));
        rspMap.put("expires_in", CacheConstants.TOKEN_TTL_SECONDS);
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
        return getSecurityUser(token, SecurityConstants.ADMIN_USER_TYPE);
    }

    /** 按指定身份域获取会话，签名类型与缓存类型必须同时匹配。 */
    public SecurityUser getSecurityUser(String token, String userType) {
        try {
            if (StringUtils.isNotBlank(token)) {
                var claims = jwtUtils.parseToken(token);
                if (!userType.equals(jwtUtils.getUserType(claims))) {
                    return null;
                }
                SecurityUser user = redisStringService.getObj(
                        getTokenKey(jwtUtils.getUserKey(claims)), SecurityUser.class);
                String cachedType = user == null || user.getUserType() == null
                        ? SecurityConstants.ADMIN_USER_TYPE : user.getUserType();
                return user != null && userType.equals(cachedType) ? user : null;
            }
        } catch (Exception ex) {
            log.warn("获取登录会话失败：{}", ex.getMessage());
        }
        return null;
    }

    /**
     * 获取用户身份信息
     */
    public SecurityUser getSecurityUserByUserKey(String userKey) {
        SecurityUser user = null;
        try {
            if (StringUtils.isNotBlank(userKey)) {
                user = redisStringService.getObj(getTokenKey(userKey), SecurityUser.class);
                if (user != null && user.getUserType() != null
                        && !SecurityConstants.ADMIN_USER_TYPE.equals(user.getUserType())) {
                    return null;
                }
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
        delSecurityUser(token, SecurityConstants.ADMIN_USER_TYPE);
    }

    /** 仅注销指定身份域的当前会话。 */
    public void delSecurityUser(String token, String userType) {
        if (StringUtils.isBlank(token)) {
            return;
        }
        try {
            var claims = jwtUtils.parseToken(token);
            if (userType.equals(jwtUtils.getUserType(claims))) {
                String userKey = jwtUtils.getUserKey(claims);
                redisStringService.delete(getTokenKey(userKey));
            }
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("注销时令牌无效：{}", ex.getMessage());
        }
    }

    /**
     * 验证会话有效期，剩余不超过 120 分钟时自动续期；时间戳单位为毫秒。
     */
    public void verifySecurityUser(SecurityUser securityUser) {
        long expireTime = securityUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= TimeUnit.SECONDS.toMillis(CacheConstants.TOKEN_REFRESH_THRESHOLD_SECONDS)) {
            refreshToken(securityUser);
        }
    }

    /**
     * 将会话续期为 30 天，Redis TTL 使用秒，过期时间使用毫秒时间戳。
     */
    public void refreshToken(SecurityUser securityUser) {
        securityUser.setLoginTime(System.currentTimeMillis());
        securityUser.setExpireTime(securityUser.getLoginTime() + TimeUnit.SECONDS.toMillis(CacheConstants.TOKEN_TTL_SECONDS));
        String userKey = getTokenKey(securityUser.getToken());
        redisStringService.setObj(userKey, securityUser, CacheConstants.TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
    }

    /** 认证中心按会员编号撤销会话，身份域隔离，不维护额外索引。 */
    public void revokeMemberSessions(Long memberId) {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("会员编号不合法");
        }
        redisStringService.scan(CacheConstants.LOGIN_TOKEN_KEY + "*", key -> {
            SecurityUser user = redisStringService.getObj(key, SecurityUser.class);
            if (user != null && SecurityConstants.MEMBER_USER_TYPE.equals(user.getUserType())
                    && memberId.equals(user.getUserId())) {
                redisStringService.delete(key);
            }
        });
    }

    /** 仅续期仍存在的会话，不能恢复已撤销的会话。 */
    public void refreshExistingToken(SecurityUser user) {
        user.setLoginTime(System.currentTimeMillis());
        user.setExpireTime(user.getLoginTime() + TimeUnit.SECONDS.toMillis(CacheConstants.TOKEN_TTL_SECONDS));
        if (!redisStringService.setObjIfPresent(getTokenKey(user.getToken()), user,
                CacheConstants.TOKEN_TTL_SECONDS, TimeUnit.SECONDS)) {
            throw new SecurityException("登录会话已失效");
        }
    }

    private String getTokenKey(String token) {
        return CacheConstants.LOGIN_TOKEN_KEY + token;
    }
}
