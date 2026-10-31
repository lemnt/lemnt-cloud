package com.lemnt.member.adapter.web.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthReqConverter;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.web.growth.req.ConfigSaveReq;
import com.lemnt.member.adapter.web.growth.res.ConfigRes;
import com.lemnt.member.application.growth.executor.MemberConfigDetailQueryExe;
import com.lemnt.member.application.growth.executor.MemberConfigSaveCmdExe;
import com.lemnt.member.application.growth.qry.ConfigDetailQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/**
 * 后台会员签到及积分配置。
 */
@Tag(name = "积分配置")
@RestController
@RequestMapping("/growths")
@RequiredArgsConstructor
public class MemberPointConfigController {

    private final MemberConfigDetailQueryExe memberConfigDetailQueryExe;
    private final MemberConfigSaveCmdExe memberConfigSaveCmdExe;

    @GetMapping("/config")
    @Operation(summary = "积分交易配置")
    @RequiresPermissions("member:config:query")
    public R<ConfigRes> config() {
        return R.ok(MemberGrowthResConverter.config(memberConfigDetailQueryExe.execute(new ConfigDetailQry())));
    }

    @PutMapping("/config")
    @Operation(summary = "保存积分交易配置")
    @RequiresPermissions("member:config:update")
    public R<Boolean> config(@Valid @RequestBody ConfigSaveReq req) {
        return R.ok(memberConfigSaveCmdExe.execute(MemberGrowthReqConverter.config(req)));
    }
}
