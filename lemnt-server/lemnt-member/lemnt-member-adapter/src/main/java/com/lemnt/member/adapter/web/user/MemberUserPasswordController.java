package com.lemnt.member.adapter.web.user;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.user.converter.MemberUserReqConverter;
import com.lemnt.member.adapter.web.user.req.PasswordResetReq;
import com.lemnt.member.application.user.executor.MemberPasswordResetCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 会员密码接口。 */
@Tag(name = "会员密码")
@RestController
@RequestMapping("/member-users")
@RequiredArgsConstructor
public class MemberUserPasswordController {

    private final MemberPasswordResetCmdExe memberPasswordResetCmdExe;

    @PutMapping("/{id}/password")
    @Operation(summary = "后台重置密码")
    @RequiresPermissions("member:user:reset-password")
    public R<Boolean> password(@PathVariable Long id, @Valid @RequestBody PasswordResetReq req) {
        return R.ok(memberPasswordResetCmdExe.execute(MemberUserReqConverter.password(id, req)));
    }
}
