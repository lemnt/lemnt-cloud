package com.lemnt.auth.application.member.service;

import com.lemnt.auth.domain.member.gateway.MemberAccountGateway;
import com.lemnt.auth.domain.member.gateway.MemberTokenGateway;
import com.lemnt.auth.domain.member.model.MemberToken;
import com.lemnt.auth.domain.user.gateway.LoginSecurityGateway;
import com.lemnt.auth.domain.user.gateway.PasswordEncoderGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

/** 会员认证编排：RPC 读取资料，复用后台密码、重试和令牌组件。 */
@Component
@RequiredArgsConstructor
public class MemberAuthenticationService {
    private final MemberAccountGateway memberAccountGateway;
    private final PasswordEncoderGateway passwordEncoderGateway;
    private final LoginSecurityGateway loginSecurityGateway;
    private final MemberTokenGateway memberTokenGateway;

    /**
     * 与既有会员手机号格式保持一致。
     */
    private static String mobile(String value) {
        if (value == null || value.isBlank() || value.length() > 24) {
            throw new IllegalArgumentException("文本为空或超长");
        }
        String phone = value.trim();
        if (!phone.matches("[+]?[0-9]{7,20}")) {
            throw new IllegalArgumentException("手机号不合法");
        }
        return phone;
    }


    /** 会员与后台使用不同重试键，按现有登录安全策略控制失败次数。 */
    public MemberToken login(String mobile, String password) { return login(mobile,password,null); }

    /** IP只由网关连接信息传入，不解析客户端代理链。 */
    public MemberToken login(String mobile, String password, String loginIp) {
        String phone = mobile(mobile);
        if (password == null || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new SecurityException("手机号或密码错误");
        }
        String retryKey = "member:" + phone;
        if (loginSecurityGateway.isLocked(retryKey)) {
            throw new SecurityException("登录尝试过多，请稍后再试");
        }
        var account = memberAccountGateway.findByMobile(phone);
        boolean matched = passwordEncoderGateway.matches(password,
                account == null ? null : account.passwordHash());
        if (!matched || account == null || account.status() != 1 || !phone.equals(account.mobile())) {
            loginSecurityGateway.incrementRetryCount(retryKey);
            throw new SecurityException("手机号或密码错误");
        }
        loginSecurityGateway.clearRetryCount(retryKey);
        var token=memberTokenGateway.create(account.memberId(), account.mobile());
        try { if(loginIp == null) memberAccountGateway.recordLogin(account.memberId());
              else memberAccountGateway.recordLogin(account.memberId(),loginIp); }
        catch (RuntimeException error) {
            memberTokenGateway.logout(token.accessToken());
            throw error;
        }
        return token;
    }

    /** 会员是否启用由认证服务根据查询结果判断，不增加远程判断接口。 */
    private boolean active(Long memberId) {
        return memberId != null
                && Integer.valueOf(1).equals(memberAccountGateway.findStatusByMemberId(memberId));
    }

    /** 校验公共会员令牌，并通过会员服务读取当前状态。 */
    public Long authenticate(String accessToken) {
        Long memberId = memberTokenGateway.memberId(accessToken);
        return active(memberId) ? memberId : null;
    }

    /** 与后台一致，续期现有访问令牌对应的 Redis 会话。 */
    public MemberToken refresh(String accessToken) {
        Long memberId = memberTokenGateway.memberId(accessToken);
        if (!active(memberId)) {
            throw new SecurityException("会员会话无效");
        }
        return memberTokenGateway.refresh(accessToken);
    }

    /** 只注销当前会员会话，不操作会员数据库。 */
    public boolean logout(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new SecurityException("会员会话无效");
        }
        memberTokenGateway.logout(authorization.substring(7));
        return true;
    }
}
