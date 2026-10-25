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

@Tag(name = "后台会员Group")
@RestController
@RequestMapping("/member/admin/groups")
@RequiredArgsConstructor
public class AdminMemberGroupController {
    private final MemberAccountCmdExe commands;
    private final MemberAccountQueryExe queries;

    @GetMapping
    @Operation(summary = "Group列表")
    @RequiresPermissions("member:group:query")
    public R<List<GroupRes>> list() {
        return R.ok(
                queries.execute(new GroupsQry()).stream().map(MemberResConverter::group).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Group详情")
    @RequiresPermissions("member:group:query")
    public R<GroupRes> detail(@PathVariable Long id) {
        return R.ok(MemberResConverter.group(queries.execute(new GroupDetailQry(id))));
    }

    @PostMapping
    @Operation(summary = "创建Group")
    @RequiresPermissions("member:group:create")
    public R<Long> create(@Valid @RequestBody GroupSaveReq req) {
        return R.ok(commands.execute(MemberReqConverter.group(null, req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改Group")
    @RequiresPermissions("member:group:update")
    public R<Long> update(@PathVariable Long id, @Valid @RequestBody GroupSaveReq req) {
        return R.ok(commands.execute(MemberReqConverter.group(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除未被会员使用的Group")
    @RequiresPermissions("member:group:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(commands.execute(new GroupDeleteCmd(id)));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "Group简洁列表")
    @RequiresPermissions("member:group:query")
    public R<List<GroupRes>> simple() {
        return R.ok(
                queries.execute(new GroupsQry()).stream()
                        .filter(v -> v.status() == 1)
                        .map(MemberResConverter::group)
                        .toList());
    }
}
