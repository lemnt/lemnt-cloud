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

@Tag(name = "后台会员Level")
@RestController
@RequestMapping("/member/admin/levels")
@RequiredArgsConstructor
public class AdminMemberLevelController {
    private final MemberAccountCmdExe commands;
    private final MemberAccountQueryExe queries;

    @GetMapping
    @Operation(summary = "Level列表")
    @RequiresPermissions("member:level:query")
    public R<List<LevelRes>> list() {
        return R.ok(
                queries.execute(new LevelsQry(false)).stream()
                        .map(MemberResConverter::level)
                        .toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Level详情")
    @RequiresPermissions("member:level:query")
    public R<LevelRes> detail(@PathVariable Long id) {
        return R.ok(MemberResConverter.level(queries.execute(new LevelDetailQry(id))));
    }

    @PostMapping
    @Operation(summary = "创建Level")
    @RequiresPermissions("member:level:create")
    public R<Long> create(@Valid @RequestBody LevelSaveReq req) {
        return R.ok(commands.execute(MemberReqConverter.level(null, req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改Level")
    @RequiresPermissions("member:level:update")
    public R<Long> update(@PathVariable Long id, @Valid @RequestBody LevelSaveReq req) {
        return R.ok(commands.execute(MemberReqConverter.level(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除未被会员使用的Level")
    @RequiresPermissions("member:level:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(commands.execute(new LevelDeleteCmd(id)));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "Level简洁列表")
    @RequiresPermissions("member:level:query")
    public R<List<LevelRes>> simple() {
        return R.ok(
                queries.execute(new LevelsQry(true)).stream()
                        .map(MemberResConverter::level)
                        .toList());
    }
}
