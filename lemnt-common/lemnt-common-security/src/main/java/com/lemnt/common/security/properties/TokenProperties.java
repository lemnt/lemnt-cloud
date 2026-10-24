package com.lemnt.common.security.properties;

import java.nio.charset.StandardCharsets;

import com.lemnt.common.core.utils.StringUtils;

import jakarta.annotation.PostConstruct;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 令牌配置
 *
 * <p>密钥不设默认值，只能由配置中心或环境变量注入：未配置或强度不足时应用启动直接失败，
 * 避免用内置密钥静默运行导致令牌可被任意伪造。</p>
 *
 * @author wangxx
 */
@Data
@ConfigurationProperties(prefix = "security.token")
public class TokenProperties {

    /**
     * HS512 要求的最小密钥长度（字节），即 512 位
     */
    public static final int MIN_SECRET_BYTES = 64;

    /**
     * JWT 签名密钥，按 UTF-8 编码后直接作为 HMAC 密钥
     */
    private String secret;

    /**
     * 校验密钥配置，配置缺失或强度不足时在启动阶段抛错
     */
    @PostConstruct
    public void validate() {
        if (StringUtils.isBlank(secret)) {
            throw new IllegalStateException(
                    "JWT 密钥未配置：请设置 security.token.secret（或环境变量 SECURITY_TOKEN_SECRET）");
        }
        int length = secret.getBytes(StandardCharsets.UTF_8).length;
        if (length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT 密钥长度不足：HS512 要求不少于 " + MIN_SECRET_BYTES + " 字节，当前为 " + length + " 字节");
        }
    }
}