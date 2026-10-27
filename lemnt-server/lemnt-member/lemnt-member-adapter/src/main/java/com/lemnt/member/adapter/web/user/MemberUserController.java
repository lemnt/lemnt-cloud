package com.lemnt.member.adapter.web.user;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.user.converter.MemberUserReqConverter;
import com.lemnt.member.adapter.web.user.converter.MemberUserResConverter;
import com.lemnt.member.adapter.web.user.req.MemberCreateReq;
import com.lemnt.member.adapter.web.user.req.MemberProfileUpdateReq;
import com.lemnt.member.adapter.web.user.req.MemberUserPageReq;
import com.lemnt.member.adapter.web.user.res.MemberUserCreateRes;
import com.lemnt.member.adapter.web.user.res.MemberUserDetailRes;
import com.lemnt.member.adapter.web.user.res.MemberUserPageRes;
import com.lemnt.member.application.user.executor.MemberDetailQueryExe;
import com.lemnt.member.application.user.executor.MemberPageQueryExe;
import com.lemnt.member.application.user.executor.MemberRegisterCmdExe;
import com.lemnt.member.application.user.executor.MemberUpdateCmdExe;
import com.lemnt.member.application.user.qry.MemberDetailQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 会员档案接口。 */
@Tag(name = "会员档案")
@RestController
@RequestMapping("/member-users")
@RequiredArgsConstructor
public class MemberUserController {

    private final MemberDetailQueryExe memberDetailQueryExe;
    private final MemberUpdateCmdExe memberUpdateCmdExe;
    private final MemberPageQueryExe memberPageQueryExe;
    private final MemberRegisterCmdExe memberRegisterCmdExe;

    @PostMapping
    @Operation(summary = "后台创建会员，不验证联系信息且不签发会话")
    @RequiresPermissions("member:user:create")
    public R<MemberUserCreateRes> create(@Valid @RequestBody MemberCreateReq req) {
        return R.ok(
                MemberUserResConverter.toMemberUserCreateRes(memberRegisterCmdExe.execute(MemberUserReqConverter.create(req))));
    }

    @GetMapping
    @Operation(summary = "会员分页，手机号精确筛选")
    @RequiresPermissions("member:user:query")
    public R<MemberUserPageRes> page(@Valid @ModelAttribute MemberUserPageReq req) {
        var page = memberPageQueryExe.execute(MemberUserReqConverter.profiles(req));
        return R.page(
                page.getRecords().stream().map(MemberUserResConverter::toMemberUserPageRes).toList(),
                page.getTotal());
    }

    @GetMapping("/{id}")
    @Operation(summary = "会员详情")
    @RequiresPermissions("member:user:query")
    public R<MemberUserDetailRes> detail(@PathVariable Long id) {
        return R.ok(MemberUserResConverter.toMemberUserDetailRes(memberDetailQueryExe.execute(new MemberDetailQry(id))));
    }

    @PutMapping("/{id}")
    @Operation(summary = "后台更新资料和状态，校验会员状态")
    @RequiresPermissions("member:user:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody MemberProfileUpdateReq req) {
        return R.ok(memberUpdateCmdExe.execute(MemberUserReqConverter.profile(id, req)));
    }
}
