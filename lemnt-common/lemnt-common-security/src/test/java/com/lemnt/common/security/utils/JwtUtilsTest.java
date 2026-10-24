package com.lemnt.common.security.utils;

import java.util.HashMap;
import java.util.Map;

import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.properties.TokenProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JWT 工具类测试
 *
 * @author wangxx
 */
class JwtUtilsTest {

    /**
     * 测试密钥，64 字节满足 HS512 的长度要求
     */
    private static final String SECRET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-!";

    /**
     * 另一个等长密钥，用于验证签名结果确实由密钥决定
     */
    private static final String OTHER_SECRET = "9876543210zyxwvutsrqponmlkjihgfedcbaZYXWVUTSRQPONMLKJIHGFEDCBA-!";

    private final JwtUtils jwtUtils = jwtUtils(SECRET);

    @Test
    @DisplayName("签发的令牌可以解出全部声明")
    void createdTokenShouldRoundTrip() {
        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.USER_KEY, "user-key-1");
        claims.put(SecurityConstants.DETAILS_USER_ID, "100");
        claims.put(SecurityConstants.DETAILS_USERNAME, "wangxx");
        claims.put(SecurityConstants.ROLE_KEY, "admin");

        Claims parsed = jwtUtils.parseToken(jwtUtils.createToken(claims));

        assertThat(jwtUtils.getUserKey(parsed)).isEqualTo("user-key-1");
        assertThat(jwtUtils.getUserId(parsed)).isEqualTo("100");
        assertThat(jwtUtils.getUserName(parsed)).isEqualTo("wangxx");
        assertThat(jwtUtils.getRoleKey(parsed)).isEqualTo("admin");
    }

    @Test
    @DisplayName("其他密钥签发的令牌无法通过校验")
    void tokenSignedWithAnotherSecretShouldBeRejected() {
        String foreignToken = jwtUtils(OTHER_SECRET)
                .createToken(Map.of(SecurityConstants.DETAILS_USER_ID, "100"));

        assertThatThrownBy(() -> jwtUtils.parseToken(foreignToken)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("被篡改的令牌无法通过校验")
    void tamperedTokenShouldBeRejected() {
        String token = jwtUtils.createToken(Map.of(SecurityConstants.DETAILS_USER_ID, "100"));
        // 改动签名段的最后一个字符
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        assertThatThrownBy(() -> jwtUtils.parseToken(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("格式非法的令牌抛出网关可识别的异常")
    void malformedTokenShouldBeRejected() {
        // AuthFilter 捕获 JwtException 与 IllegalArgumentException，此处必须落在该范围内
        assertThatThrownBy(() -> jwtUtils.parseToken("not-a-jwt"))
                .isInstanceOfAny(JwtException.class, IllegalArgumentException.class);
    }

    @Test
    @DisplayName("声明缺失时返回空串而不是 null")
    void missingClaimShouldReturnEmptyString() {
        Claims parsed = jwtUtils.parseToken(jwtUtils.createToken(Map.of(SecurityConstants.DETAILS_USER_ID, "100")));

        assertThat(jwtUtils.getUserName(parsed)).isEmpty();
    }

    private static JwtUtils jwtUtils(String secret) {
        TokenProperties properties = new TokenProperties();
        properties.setSecret(secret);
        return new JwtUtils(properties);
    }
}