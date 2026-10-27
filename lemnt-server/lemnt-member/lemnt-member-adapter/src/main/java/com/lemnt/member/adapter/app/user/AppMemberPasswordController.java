package com.lemnt.member.adapter.app.user;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.member.adapter.app.user.converter.MemberUserReqConverter;
import com.lemnt.member.adapter.app.user.req.PasswordChangeReq;
import com.lemnt.member.application.user.executor.MemberPasswordChangeCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 小程序会员密码接口。 */
@Tag(name = "小程序会员密码")
@RestController
@RequestMapping("/app/users")
@RequiredArgsConstructor
public class AppMemberPasswordController {

    private final MemberPasswordChangeCmdExe memberPasswordChangeCmdExe;

    @PutMapping("/user/password")
    @Operation(summary = "验证原密码并修改")
    public R<Boolean> password(
            @Valid @RequestBody PasswordChangeReq req) {
        return R.ok(memberPasswordChangeCmdExe.execute(MemberUserReqConverter.password(SecurityUtils.getUserId(), req)));
    }
}
