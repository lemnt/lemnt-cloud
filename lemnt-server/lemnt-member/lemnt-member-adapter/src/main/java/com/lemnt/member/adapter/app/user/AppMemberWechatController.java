package com.lemnt.member.adapter.app.user;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.member.adapter.app.user.converter.MemberUserReqConverter;
import com.lemnt.member.adapter.app.user.req.WechatLoginReq;
import com.lemnt.member.application.user.executor.MemberWechatLoginCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/**
 * 小程序微信登录接口，登录结果只绑定微信标识，不签发令牌。
 */
@Tag(name = "小程序微信登录")
@RestController
@RequestMapping("/app/wechat")
@RequiredArgsConstructor
public class AppMemberWechatController {

    private final MemberWechatLoginCmdExe memberWechatLoginCmdExe;

    @PostMapping("/login")
    @Operation(summary = "用小程序登录凭证换取并绑定微信 openid")
    public R<Boolean> login(@Valid @RequestBody WechatLoginReq req) {
        return R.ok(
            memberWechatLoginCmdExe.execute(
                MemberUserReqConverter.wechatLogin(SecurityUtils.getUserId(), req)));
    }
}
