package com.lemnt.member.adapter.web.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthReqConverter;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.web.growth.req.MemberLevelCreateReq;
import com.lemnt.member.adapter.web.growth.req.MemberLevelUpdateReq;
import com.lemnt.member.adapter.web.growth.res.MemberLevelDetailRes;
import com.lemnt.member.adapter.web.growth.res.MemberLevelListRes;
import com.lemnt.member.adapter.web.growth.res.MemberLevelSimpleRes;
import com.lemnt.member.application.growth.cmd.LevelDeleteCmd;
import com.lemnt.member.application.growth.executor.MemberLevelDeleteCmdExe;
import com.lemnt.member.application.growth.executor.MemberLevelDetailQueryExe;
import com.lemnt.member.application.growth.executor.MemberLevelListQueryExe;
import com.lemnt.member.application.growth.executor.MemberLevelSaveCmdExe;
import com.lemnt.member.application.growth.qry.LevelDetailQry;
import com.lemnt.member.application.growth.qry.LevelListQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import com.lemnt.member.adapter.web.growth.req.MemberLevelListReq;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.List;

/**
 * 后台会员等级接口。
 */
@Tag(name = "后台会员Level")
@RestController
@RequestMapping("/growth/levels")
@RequiredArgsConstructor
public class MemberLevelController {

    private final MemberLevelDeleteCmdExe memberLevelDeleteCmdExe;
    private final MemberLevelDetailQueryExe memberLevelDetailQueryExe;
    private final MemberLevelSaveCmdExe memberLevelSaveCmdExe;
    private final MemberLevelListQueryExe memberLevelListQueryExe;

    @GetMapping
    @Operation(summary = "Level列表")
    @RequiresPermissions("member:level:query")
    public R<List<MemberLevelListRes>> list(@Valid @ModelAttribute MemberLevelListReq req) {
        return R.ok(
                memberLevelListQueryExe.execute(new LevelListQry(false, req.name(), req.status())).stream()
                .map(MemberGrowthResConverter::toMemberLevelListRes)
                .toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Level详情")
    @RequiresPermissions("member:level:query")
    public R<MemberLevelDetailRes> detail(@PathVariable Long id) {
        return R.ok(MemberGrowthResConverter.toMemberLevelDetailRes(memberLevelDetailQueryExe.execute(new LevelDetailQry(id))));
    }

    @PostMapping
    @Operation(summary = "创建Level")
    @RequiresPermissions("member:level:create")
    public R<Long> create(@Valid @RequestBody MemberLevelCreateReq req) {
        return R.ok(memberLevelSaveCmdExe.execute(MemberGrowthReqConverter.level(null, req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改Level")
    @RequiresPermissions("member:level:update")
    public R<Long> update(@PathVariable Long id, @Valid @RequestBody MemberLevelUpdateReq req) {
        return R.ok(memberLevelSaveCmdExe.execute(MemberGrowthReqConverter.level(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除未被会员使用的Level")
    @RequiresPermissions("member:level:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(memberLevelDeleteCmdExe.execute(new LevelDeleteCmd(id)));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "Level简洁列表")
    @RequiresPermissions("member:level:query")
    public R<List<MemberLevelSimpleRes>> simple() {
        return R.ok(
                memberLevelListQueryExe.execute(new LevelListQry(true)).stream()
                .map(MemberGrowthResConverter::toMemberLevelSimpleRes)
                .toList());
    }
}
