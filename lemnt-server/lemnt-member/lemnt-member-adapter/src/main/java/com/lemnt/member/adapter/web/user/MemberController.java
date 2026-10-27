package com.lemnt.member.adapter.web.user;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.user.converter.MemberUserReqConverter;
import com.lemnt.member.adapter.web.user.converter.MemberUserResConverter;
import com.lemnt.member.adapter.web.user.req.MemberRegisterReq;
import com.lemnt.member.adapter.web.user.res.MemberDetailRes;
import com.lemnt.member.adapter.web.user.res.MemberRegisterRes;
import com.lemnt.member.application.user.executor.MemberDetailQueryExe;
import com.lemnt.member.application.user.executor.MemberRegisterCmdExe;
import com.lemnt.member.application.user.qry.MemberDetailQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 兼容既有会员资料入口。 */
@Tag(name = "会员基础资料")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberDetailQueryExe memberDetailQueryExe;
    private final MemberRegisterCmdExe memberRegisterCmdExe;

    /** 创建会员资料，不签发登录会话。 */
    @PostMapping("/register")
    @RequiresPermissions("member:user:create")
    @Operation(summary = "创建会员")
    public R<MemberRegisterRes> register(@Valid @RequestBody MemberRegisterReq req) {
        return R.ok(MemberUserResConverter.toMemberRegisterRes(
                memberRegisterCmdExe.execute(MemberUserReqConverter.toRegisterCmd(req))));
    }

    /** 根据会员编号查询基础资料。 */
    @GetMapping("/{id}")
    @RequiresPermissions("member:query")
    @Operation(summary = "会员详情")
    public R<MemberDetailRes> detail(@PathVariable Long id) {
        var member = memberDetailQueryExe.execute(new MemberDetailQry(id));
        return member == null ? R.fail("会员不存在") : R.ok(MemberUserResConverter.toMemberDetailRes(member));
    }
}
