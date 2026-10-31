package com.lemnt.member.adapter.web.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthReqConverter;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.web.growth.req.SignConfigCreateReq;
import com.lemnt.member.adapter.web.growth.req.SignConfigUpdateReq;
import com.lemnt.member.adapter.web.growth.res.MemberConfigDetailRes;
import com.lemnt.member.adapter.web.growth.res.MemberConfigListRes;
import com.lemnt.member.application.growth.cmd.SignConfigDeleteCmd;
import com.lemnt.member.application.growth.executor.MemberSignConfigDeleteCmdExe;
import com.lemnt.member.application.growth.executor.MemberSignConfigDetailQueryExe;
import com.lemnt.member.application.growth.executor.MemberSignConfigListQueryExe;
import com.lemnt.member.application.growth.executor.MemberSignConfigCreateCmdExe;
import com.lemnt.member.application.growth.executor.MemberSignConfigUpdateCmdExe;
import com.lemnt.member.application.growth.qry.SignConfigDetailQry;
import com.lemnt.member.application.growth.qry.SignConfigListQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 后台会员签到及积分配置。
 */
@Tag(name = "签到规则")
@RestController
@RequestMapping("/growths")
@RequiredArgsConstructor
public class MemberSignConfigController {

    private final MemberSignConfigDeleteCmdExe memberSignConfigDeleteCmdExe;
    private final MemberSignConfigDetailQueryExe memberSignConfigDetailQueryExe;
    private final MemberSignConfigCreateCmdExe memberSignConfigCreateCmdExe;
    private final MemberSignConfigUpdateCmdExe memberSignConfigUpdateCmdExe;
    private final MemberSignConfigListQueryExe memberSignConfigListQueryExe;

    @GetMapping("/signin-configs")
    @Operation(summary = "所有签到规则")
    @RequiresPermissions("member:sign-in-config:query")
    public R<List<MemberConfigListRes>> list() {
        return R.ok(
                memberSignConfigListQueryExe.execute(new SignConfigListQry(false)).stream()
                .map(MemberGrowthResConverter::toMemberConfigListRes)
                .toList());
    }

    @PostMapping("/signin-configs")
    @Operation(summary = "新增签到规则，天数重复拒绝")
    @RequiresPermissions("member:sign-in-config:create")
    public R<Long> create(@Valid @RequestBody SignConfigCreateReq req) {
        return R.ok(memberSignConfigCreateCmdExe.execute(MemberGrowthReqConverter.signConfig(req)));
    }

    @PutMapping("/signin-configs/{id}")
    @Operation(summary = "按ID修改签到规则，允许调整天数")
    @RequiresPermissions("member:sign-in-config:update")
    public R<Boolean> update(@PathVariable Long id, @Valid @RequestBody SignConfigUpdateReq req) {
        return R.ok(memberSignConfigUpdateCmdExe.execute(MemberGrowthReqConverter.signConfig(id, req)));
    }

    @DeleteMapping("/signin-configs/{id}")
    @Operation(summary = "按ID删除规则，历史奖励保留")
    @RequiresPermissions("member:sign-in-config:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(memberSignConfigDeleteCmdExe.execute(new SignConfigDeleteCmd(id)));
    }

    @GetMapping("/signin-configs/{id}")
    @Operation(summary = "按ID读取签到规则")
    @RequiresPermissions("member:sign-in-config:query")
    public R<MemberConfigDetailRes> detail(@PathVariable Long id) {
        return R.ok(MemberGrowthResConverter.toMemberConfigDetailRes(
                memberSignConfigDetailQueryExe.execute(new SignConfigDetailQry(id))));
    }
}
