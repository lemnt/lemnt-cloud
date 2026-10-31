package com.lemnt.auth.infrastructure.user.gateway;

import com.lemnt.auth.domain.user.entity.LoginUser;
import com.lemnt.auth.domain.user.entity.TokenResult;
import com.lemnt.auth.domain.user.gateway.TokenGateway;
import com.lemnt.auth.infrastructure.user.converter.TokenConverter;
import com.lemnt.common.security.auth.AuthUtil;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.service.TokenService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Token 网关实现（基于 JWT + Redis）
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class TokenGatewayImpl implements TokenGateway {
    private final TokenService tokenService;

    @Override
    public TokenResult createToken(LoginUser loginUser) {
        var result = tokenService.createToken(TokenConverter.toCommon(loginUser));
        return new TokenResult(
                (String) result.get("access_token"),
                (Long) result.get("expires_in")
        );
    }

    @Override
    public void refreshToken(LoginUser loginUser) {
        tokenService.refreshToken(TokenConverter.toCommon(loginUser));
    }

    @Override
    public LoginUser getLoginUser(String token) {
        SecurityUser common = tokenService.getSecurityUser(token);
        return common != null ? TokenConverter.toDomain(common) : null;
    }

    @Override
    public void logoutByToken(String token) {
        AuthUtil.logoutByToken(token);
    }
}
