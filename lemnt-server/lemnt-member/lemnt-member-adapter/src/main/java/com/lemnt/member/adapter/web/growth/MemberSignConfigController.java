package com.lemnt.member.adapter.web.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthReqConverter;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.web.growth.req.SignConfigSaveReq;
import com.lemnt.member.adapter.web.growth.res.MemberConfigDetailRes;
import com.lemnt.member.adapter.web.growth.res.MemberConfigListRes;
import com.lemnt.member.application.growth.cmd.SignConfigDeleteCmd;
import com.lemnt.member.application.growth.executor.MemberSignConfigDeleteCmdExe;
import com.lemnt.member.application.growth.executor.MemberSignConfigDetailQueryExe;
import com.lemnt.member.application.growth.executor.MemberSignConfigListQueryExe;
import com.lemnt.member.application.growth.executor.MemberSignConfigSaveCmdExe;
import com.lemnt.member.application.growth.qry.SignConfigDetailQry;
import com.lemnt.member.application.growth.qry.SignConfigListQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final MemberSignConfigSaveCmdExe memberSignConfigSaveCmdExe;
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

    @PutMapping("/signin-configs")
    @Operation(summary = "按签到天数创建或更新规则，1启用0停用")
    @RequiresPermissions("member:sign-in-config:update")
    public R<Boolean> signConfig(@Valid @RequestBody SignConfigSaveReq req) {
        return R.ok(memberSignConfigSaveCmdExe.execute(MemberGrowthReqConverter.signConfig(req)));
    }

    @DeleteMapping("/signin-configs/{day}")
    @Operation(summary = "删除指定天数规则，历史奖励保留")
    @RequiresPermissions("member:sign-in-config:delete")
    public R<Boolean> delete(@PathVariable Integer day) {
        return R.ok(memberSignConfigDeleteCmdExe.execute(new SignConfigDeleteCmd(day)));
    }

    @GetMapping("/signin-configs/{day}")
    @Operation(summary = "指定签到天数规则")
    @RequiresPermissions("member:sign-in-config:query")
    public R<MemberConfigDetailRes> detail(@PathVariable Integer day) {
        return R.ok(MemberGrowthResConverter.toMemberConfigDetailRes(memberSignConfigDetailQueryExe.execute(new SignConfigDetailQry(day))));
    }
}
