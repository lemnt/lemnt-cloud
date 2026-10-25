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
import java.util.List;
import com.lemnt.common.security.annotation.RequiresPermissions;

@Tag(name = "后台会员管理")
@RestController
@RequestMapping("/member/admin/users")
@RequiredArgsConstructor
public class AdminMemberUserController {
    private final MemberAccountCmdExe commands;
    private final MemberAccountQueryExe queries;
    private final MemberAuthCmdExe authentication;
    private final com.lemnt.member.application.executor.MemberRegisterCmdExe registration;

    @PostMapping
    @Operation(summary = "后台创建会员，不验证联系信息且不签发会话")
    @RequiresPermissions("member:user:create")
    public R<ProfileRes> create(@Valid @RequestBody MemberCreateReq req) {
        return R.ok(
                MemberResConverter.profile(registration.execute(MemberReqConverter.create(req))));
    }

    @GetMapping
    @Operation(summary = "会员分页，手机号精确筛选")
    @RequiresPermissions("member:user:query")
    public R<AccountPageRes<ProfileRes>> page(@Valid @ModelAttribute AccountPageReq req) {
        return R.ok(
                MemberResConverter.page(
                        queries.execute(MemberReqConverter.profiles(req)),
                        MemberResConverter::profile));
    }

    @GetMapping("/{id}")
    @Operation(summary = "会员详情")
    @RequiresPermissions("member:user:query")
    public R<ProfileRes> detail(@PathVariable Long id) {
        return R.ok(MemberResConverter.profile(queries.execute(new ProfileQry(id))));
    }

    @PutMapping("/{id}")
    @Operation(summary = "后台更新资料和状态，停用立即撤销会话")
    @RequiresPermissions("member:user:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody AdminProfileUpdateReq req) {
        return R.ok(commands.execute(MemberReqConverter.profile(id, req)));
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "后台重置密码，撤销全部会话")
    @RequiresPermissions("member:user:reset-password")
    public R<Boolean> password(@PathVariable Long id, @Valid @RequestBody PasswordResetReq req) {
        return R.ok(authentication.execute(MemberReqConverter.password(id, req)));
    }

    @PutMapping("/{id}/level")
    @Operation(summary = "调整等级及对应经验，幂等业务编号")
    @RequiresPermissions("member:user:update-level")
    public R<Boolean> level(@PathVariable Long id, @Valid @RequestBody LevelAssignReq req) {
        return R.ok(commands.execute(MemberReqConverter.level(id, req)));
    }

    @PutMapping("/{id}/group")
    @Operation(summary = "分配或清空分组")
    @RequiresPermissions("member:user:update")
    public R<Boolean> group(@PathVariable Long id, @Valid @RequestBody GroupAssignReq req) {
        return R.ok(commands.execute(MemberReqConverter.group(id, req)));
    }

    @GetMapping("/{id}/tags")
    @Operation(summary = "会员标签编号")
    @RequiresPermissions("member:user:query")
    public R<List<Long>> tags(@PathVariable Long id) {
        return R.ok(queries.execute(new MemberTagsQry(id)));
    }

    @PutMapping("/{id}/tags")
    @Operation(summary = "设置标签集合，空集合清空")
    @RequiresPermissions("member:user:update")
    public R<Boolean> tags(@PathVariable Long id, @Valid @RequestBody TagsAssignReq req) {
        return R.ok(commands.execute(MemberReqConverter.tags(id, req)));
    }

    @GetMapping("/{id}/addresses")
    @Operation(summary = "后台查看会员地址簿，包含个人信息")
    @RequiresPermissions("member:address:query")
    public R<List<AddressRes>> addresses(@PathVariable Long id) {
        return R.ok(
                queries.execute(new AddressListQry(id)).stream()
                        .map(MemberResConverter::address)
                        .toList());
    }

    @GetMapping("/{id}/level-records")
    @Operation(summary = "等级变更记录分页")
    @RequiresPermissions("member:level-record:query")
    public R<AccountPageRes<LevelRecordRes>> levelRecords(
            @PathVariable Long id, @Valid @ModelAttribute AccountPageReq req) {
        return R.ok(
                MemberResConverter.page(
                        queries.execute(MemberReqConverter.levelRecords(id, req)),
                        MemberResConverter::levelRecord));
    }

    @GetMapping("/{id}/signin-records")
    @Operation(summary = "会员签到记录分页")
    @RequiresPermissions("member:sign-in-record:query")
    public R<AccountPageRes<SignRecordRes>> signs(
            @PathVariable Long id, @Valid @ModelAttribute AccountPageReq req) {
        return R.ok(
                MemberResConverter.page(
                        queries.execute(MemberReqConverter.signs(id, req)),
                        MemberResConverter::signRecord));
    }

    @PutMapping("/{id}/points")
    @Operation(summary = "幂等变更会员points，余额不能为负")
    @RequiresPermissions("member:user:update-point")
    public R<AssetRecordRes> points(@PathVariable Long id, @Valid @RequestBody AssetAdjustReq req) {
        return R.ok(
                MemberResConverter.assetRecord(
                        commands.execute(MemberReqConverter.asset(id, "POINT", req))));
    }

    @GetMapping("/{id}/points-records")
    @Operation(summary = "会员points流水分页")
    @RequiresPermissions("member:point-record:query")
    public R<AccountPageRes<AssetRecordRes>> pointsRecords(
            @PathVariable Long id, @Valid @ModelAttribute AccountPageReq req) {
        return R.ok(
                MemberResConverter.page(
                        queries.execute(MemberReqConverter.assets(id, "POINT", req)),
                        MemberResConverter::assetRecord));
    }

    @PutMapping("/{id}/experience")
    @Operation(summary = "幂等变更会员experience，余额不能为负")
    @RequiresPermissions("member:user:update-level")
    public R<AssetRecordRes> experience(
            @PathVariable Long id, @Valid @RequestBody AssetAdjustReq req) {
        return R.ok(
                MemberResConverter.assetRecord(
                        commands.execute(MemberReqConverter.asset(id, "EXPERIENCE", req))));
    }

    @GetMapping("/{id}/experience-records")
    @Operation(summary = "会员experience流水分页")
    @RequiresPermissions("member:experience-record:query")
    public R<AccountPageRes<AssetRecordRes>> experienceRecords(
            @PathVariable Long id, @Valid @ModelAttribute AccountPageReq req) {
        return R.ok(
                MemberResConverter.page(
                        queries.execute(MemberReqConverter.assets(id, "EXPERIENCE", req)),
                        MemberResConverter::assetRecord));
    }
}
