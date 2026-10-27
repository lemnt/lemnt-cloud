package com.lemnt.member.adapter.app.user;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.member.adapter.app.user.converter.MemberUserReqConverter;
import com.lemnt.member.adapter.app.user.converter.MemberUserResConverter;
import com.lemnt.member.adapter.app.user.req.ProfileUpdateReq;
import com.lemnt.member.adapter.app.user.res.ProfileRes;
import com.lemnt.member.application.user.executor.MemberDetailQueryExe;
import com.lemnt.member.application.user.executor.MemberUpdateCmdExe;
import com.lemnt.member.application.user.qry.MemberDetailQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 小程序会员档案接口。 */
@Tag(name = "小程序会员档案")
@RestController
@RequestMapping("/app/users")
@RequiredArgsConstructor
public class AppMemberUserController {

    private final MemberDetailQueryExe memberDetailQueryExe;
    private final MemberUpdateCmdExe memberUpdateCmdExe;

    @GetMapping("/user")
    @Operation(summary = "当前会员资料")
    public R<ProfileRes> user() {
        return R.ok(
                MemberUserResConverter.profile(memberDetailQueryExe.execute(new MemberDetailQry(SecurityUtils.getUserId()))));
    }

    @PutMapping("/user")
    @Operation(summary = "更新本人资料，不接受会员ID、手机号、积分、等级或状态")
    public R<Boolean> update(
            @Valid @RequestBody ProfileUpdateReq req) {
        return R.ok(memberUpdateCmdExe.execute(MemberUserReqConverter.profile(SecurityUtils.getUserId(), req)));
    }
}
