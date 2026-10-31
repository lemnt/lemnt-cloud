package com.lemnt.common.security.utils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import cn.hutool.core.convert.Convert;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.properties.TokenProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

/**
 * Jwt 工具类
 *
 * <p>由 {@code TokenConfig} 注册为单例 Bean，密钥在构造时从 {@link TokenProperties} 读取并校验，
 * 因此不再持有静态状态，密钥非法时应用无法启动。</p>
 *
 * @author wangxx
 */
public class JwtUtils {

    private final SecretKey secretKey;

    /** 读取签名内的身份类型；兼容未携带该字段的历史后台令牌。 */
    public String getUserType(Claims claims) {
        String userType = claims.get(SecurityConstants.USER_TYPE, String.class);
        return userType == null ? SecurityConstants.ADMIN_USER_TYPE : userType;
    }


    public JwtUtils(TokenProperties tokenProperties) {
        this.secretKey = Keys.hmacShaKeyFor(tokenProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从数据声明生成令牌，不设置固定 exp；有效期由 Redis 滑动会话控制。
     *
     * @param claims 数据声明
     * @return 令牌
     */
    public String createToken(Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 根据令牌获取用户标识
     *
     * @param token 令牌
     * @return 用户 ID
     */
    public String getUserKey(String token) {
        Claims claims = parseToken(token);
        return getValue(claims, SecurityConstants.USER_KEY);
    }

    /**
     * 根据令牌获取用户标识
     *
     * @param claims 身份信息
     * @return 用户 ID
     */
    public String getUserKey(Claims claims) {
        return getValue(claims, SecurityConstants.USER_KEY);
    }

    /**
     * 根据令牌获取用户 ID
     *
     * @param token 令牌
     * @return 用户 ID
     */
    public String getUserId(String token) {
        Claims claims = parseToken(token);
        return getValue(claims, SecurityConstants.DETAILS_USER_ID);
    }

    /**
     * 根据身份信息获取用户 ID
     *
     * @param claims 身份信息
     * @return 用户 ID
     */
    public String getUserId(Claims claims) {
        return getValue(claims, SecurityConstants.DETAILS_USER_ID);
    }

    /**
     * 根据身份信息获取角色
     *
     * @param claims 身份信息
     * @return 用户 ID
     */
    public String getRoleKey(Claims claims) {
        return getValue(claims, SecurityConstants.ROLE_KEY);
    }

    /**
     * 根据令牌获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUserName(String token) {
        Claims claims = parseToken(token);
        return getValue(claims, SecurityConstants.DETAILS_USERNAME);
    }

    /**
     * 根据身份信息获取用户名
     *
     * @param claims 身份信息
     * @return 用户名
     */
    public String getUserName(Claims claims) {
        return getValue(claims, SecurityConstants.DETAILS_USERNAME);
    }

    /**
     * 根据身份信息获取键值
     *
     * @param claims 身份信息
     * @param key    键
     * @return 值
     */
    public String getValue(Claims claims, String key) {
        return Convert.toStr(claims.get(key), "");
    }
}