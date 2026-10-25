package com.lemnt.member.adapter.web.account;

import com.lemnt.common.core.domain.R;
import com.lemnt.member.adapter.web.account.req.*;
import com.lemnt.member.adapter.web.account.res.*;
import com.lemnt.member.adapter.web.account.converter.*;
import com.lemnt.member.application.account.cmd.*;
import com.lemnt.member.application.account.qry.*;
import com.lemnt.member.application.account.executor.*;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "小程序会员认证")
@RestController
@RequestMapping("/member/app/auth")
@RequiredArgsConstructor
public class AppMemberAuthController {
    private final MemberAuthCmdExe commands;

    @PostMapping("/login")
    @Operation(summary = "会员手机号密码登录，独立令牌域")
    public R<TokenPairRes> login(@Valid @RequestBody AuthLoginReq req) {
        return R.ok(MemberResConverter.tokenPair(commands.execute(MemberReqConverter.login(req))));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "一次性刷新轮换，旧访问和刷新令牌同时失效")
    public R<TokenPairRes> refresh(@Valid @RequestBody AuthRefreshReq req) {
        return R.ok(
                MemberResConverter.tokenPair(commands.execute(MemberReqConverter.refresh(req))));
    }

    @PostMapping("/logout")
    @Operation(summary = "撤销当前会员会话，重复退出幂等")
    public R<Boolean> logout(
            @RequestHeader(value = "Authorization", required = false) String token) {
        return R.ok(commands.execute(new AuthLogoutCmd(token)));
    }
}
