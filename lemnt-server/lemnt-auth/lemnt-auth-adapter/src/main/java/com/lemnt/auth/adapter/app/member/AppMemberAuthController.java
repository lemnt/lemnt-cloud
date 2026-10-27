package com.lemnt.auth.adapter.app.member;

import com.lemnt.auth.adapter.app.member.converter.MemberAuthReqConverter;
import com.lemnt.auth.adapter.app.member.converter.MemberAuthResConverter;
import com.lemnt.auth.adapter.app.member.req.AuthLoginReq;
import com.lemnt.auth.adapter.app.member.res.AuthAppMemberAuthLoginRes;
import com.lemnt.auth.adapter.app.member.res.AuthAppMemberAuthRefreshRes;
import com.lemnt.auth.application.member.cmd.AuthLogoutCmd;
import com.lemnt.auth.application.member.cmd.AuthRefreshCmd;
import com.lemnt.auth.application.member.executor.MemberAuthCmdExe;
import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 小程序会员认证。
 */
@Tag(name = "小程序会员认证")
@RestController
@RequestMapping("/member/app/auth")
@RequiredArgsConstructor
public class AppMemberAuthController {
    private final MemberAuthCmdExe memberAuthCmdExe;

    @PostMapping("/login")
    @Operation(summary = "会员手机号密码登录，独立令牌域")
    public R<AuthAppMemberAuthLoginRes> login(@Valid @RequestBody AuthLoginReq req) {
        return R.ok(MemberAuthResConverter.toResToAuthAppMemberAuthLoginRes(memberAuthCmdExe.execute(MemberAuthReqConverter.toLoginCmd(req))));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "续期当前会员登录会话")
    public R<AuthAppMemberAuthRefreshRes> refresh(HttpServletRequest request) {
        return R.ok(MemberAuthResConverter.toResToAuthAppMemberAuthRefreshRes(memberAuthCmdExe.execute(
                new AuthRefreshCmd(SecurityUtils.getToken(request)))));
    }

    @PostMapping("/logout")
    @Operation(summary = "撤销当前会员会话，重复退出幂等")
    public R<Boolean> logout(
            @RequestHeader(value = "Authorization", required = false) String token) {
        return R.ok(memberAuthCmdExe.execute(new AuthLogoutCmd(token)));
    }
}
