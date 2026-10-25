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

@Tag(name = "后台会员签到及积分配置")
@RestController
@RequestMapping("/member/admin")
@RequiredArgsConstructor
public class AdminMemberConfigController {
    private final MemberAccountCmdExe commands;
    private final MemberAccountQueryExe queries;

    @GetMapping("/config")
    @Operation(summary = "积分交易配置，交易应用另行接入")
    @RequiresPermissions("member:config:query")
    public R<ConfigRes> config() {
        return R.ok(MemberResConverter.config(queries.execute(new ConfigQry())));
    }

    @PutMapping("/config")
    @Operation(summary = "保存积分交易配置")
    @RequiresPermissions("member:config:update")
    public R<Boolean> config(@Valid @RequestBody ConfigSaveReq req) {
        return R.ok(commands.execute(MemberReqConverter.config(req)));
    }

    @GetMapping("/signin-configs")
    @Operation(summary = "所有签到规则")
    @RequiresPermissions("member:sign-in-config:query")
    public R<List<SignConfigRes>> list() {
        return R.ok(
                queries.execute(new SignConfigsQry(false)).stream()
                        .map(MemberResConverter::signConfig)
                        .toList());
    }

    @PutMapping("/signin-configs")
    @Operation(summary = "按签到天数创建或更新规则，1启用0停用")
    @RequiresPermissions("member:sign-in-config:update")
    public R<Boolean> signConfig(@Valid @RequestBody SignConfigSaveReq req) {
        return R.ok(commands.execute(MemberReqConverter.signConfig(req)));
    }

    @DeleteMapping("/signin-configs/{day}")
    @Operation(summary = "删除指定天数规则，历史奖励保留")
    @RequiresPermissions("member:sign-in-config:delete")
    public R<Boolean> delete(@PathVariable Integer day) {
        return R.ok(commands.execute(new SignConfigDeleteCmd(day)));
    }

    @GetMapping("/signin-configs/{day}")
    @Operation(summary = "指定签到天数规则")
    @RequiresPermissions("member:sign-in-config:query")
    public R<SignConfigRes> detail(@PathVariable Integer day) {
        return R.ok(MemberResConverter.signConfig(queries.execute(new SignConfigDetailQry(day))));
    }
}
