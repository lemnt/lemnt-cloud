package com.lemnt.auth.infrastructure.user.gateway;

import com.lemnt.auth.domain.user.gateway.PasswordEncoderGateway;
import com.lemnt.common.security.utils.SecurityUtils;

import org.springframework.stereotype.Component;

/**
 * 密码编码器实现（基于 Spring Security BCrypt）
 *
 * @author wangxx
 */
@Component
public class PasswordEncoderGatewayImpl implements PasswordEncoderGateway {
    // 缺少凭证时仍执行同等成本的默认算法校验，避免暴露账户是否存在。
    private final String dummyHash = SecurityUtils.encryptPassword(java.util.UUID.randomUUID().toString());

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return SecurityUtils.matchesPassword(rawPassword, encodedPassword == null ? dummyHash : encodedPassword);
    }

    @Override
    public String encode(String rawPassword) {
        return SecurityUtils.encryptPassword(rawPassword);
    }
}
