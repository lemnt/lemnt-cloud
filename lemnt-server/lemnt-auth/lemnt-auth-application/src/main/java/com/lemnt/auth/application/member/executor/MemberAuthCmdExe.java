package com.lemnt.auth.application.member.executor;

import com.lemnt.auth.application.member.cmd.AuthLoginCmd;
import com.lemnt.auth.application.member.cmd.AuthLogoutCmd;
import com.lemnt.auth.application.member.cmd.AuthRefreshCmd;
import com.lemnt.auth.application.member.dto.MemberTokenDTO;
import com.lemnt.auth.application.member.service.MemberAuthenticationService;
import com.lemnt.auth.domain.member.model.MemberToken;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 小程序认证命令入口，直接调用认证中心本地服务。
 */
@Component
@RequiredArgsConstructor
public class MemberAuthCmdExe {

    private final MemberAuthenticationService memberAuthenticationService;

    /**
     * 校验会员密码并签发独立会话。
     */
    public MemberTokenDTO execute(AuthLoginCmd cmd) {
        return result(cmd.loginIp() == null ? memberAuthenticationService.login(cmd.mobile(), cmd.password())
                : memberAuthenticationService.login(cmd.mobile(), cmd.password(), cmd.loginIp()));
    }

    /**
     * 续期当前会员会话。
     */
    public MemberTokenDTO execute(AuthRefreshCmd cmd) {
        return result(memberAuthenticationService.refresh(cmd.accessToken()));
    }

    /**
     * 撤销当前会话。
     */
    public Boolean execute(AuthLogoutCmd cmd) {
        return memberAuthenticationService.logout(cmd.authorization());
    }

    private MemberTokenDTO result(MemberToken value) {
        return new MemberTokenDTO(value.memberId(), value.accessToken(), value.expiresIn());
    }
}
