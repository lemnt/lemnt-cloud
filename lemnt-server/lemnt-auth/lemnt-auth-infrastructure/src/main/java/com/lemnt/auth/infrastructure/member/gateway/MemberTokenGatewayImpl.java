package com.lemnt.auth.infrastructure.member.gateway;

import com.lemnt.auth.domain.member.gateway.MemberTokenGateway;
import com.lemnt.auth.domain.member.model.MemberToken;
import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.common.security.model.SecurityUserDetail;
import com.lemnt.common.security.service.TokenService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 复用后台 TokenService，不自行生成、签名或解析令牌。
 */
@Component
@RequiredArgsConstructor
public class MemberTokenGatewayImpl implements MemberTokenGateway {

    private final TokenService tokenService;

    @Override
    public MemberToken create(Long memberId, String username) {
        SecurityUser user = new SecurityUser();
        user.setUserType(SecurityConstants.MEMBER_USER_TYPE);
        user.setDetail(new SecurityUserDetail().setUserId(memberId).setUserName(username));
        user.setRoles(Set.of());
        user.setPermissions(Set.of());
        var token = tokenService.createToken(user);
        return new MemberToken(memberId, (String) token.get("access_token"), (Long) token.get("expires_in"));
    }

    @Override
    public Long memberId(String accessToken) {
        SecurityUser user = tokenService.getSecurityUser(accessToken, SecurityConstants.MEMBER_USER_TYPE);
        return user == null ? null : user.getUserId();
    }

    @Override
    public MemberToken refresh(String accessToken) {
        SecurityUser user = tokenService.getSecurityUser(accessToken, SecurityConstants.MEMBER_USER_TYPE);
        if (user == null) {
            throw new SecurityException("会员会话无效");
        }
        tokenService.refreshToken(user);
        return new MemberToken(user.getUserId(), accessToken, CacheConstants.TOKEN_TTL_SECONDS);
    }

    @Override
    public void logout(String accessToken) {
        tokenService.delSecurityUser(accessToken, SecurityConstants.MEMBER_USER_TYPE);
    }
}
