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

@Tag(name = "后台会员Tag")
@RestController
@RequestMapping("/member/admin/tags")
@RequiredArgsConstructor
public class AdminMemberTagController {
    private final MemberAccountCmdExe commands;
    private final MemberAccountQueryExe queries;

    @GetMapping
    @Operation(summary = "Tag列表")
    @RequiresPermissions("member:tag:query")
    public R<List<TagRes>> list() {
        return R.ok(queries.execute(new TagsQry()).stream().map(MemberResConverter::tag).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tag详情")
    @RequiresPermissions("member:tag:query")
    public R<TagRes> detail(@PathVariable Long id) {
        return R.ok(MemberResConverter.tag(queries.execute(new TagDetailQry(id))));
    }

    @PostMapping
    @Operation(summary = "创建Tag")
    @RequiresPermissions("member:tag:create")
    public R<Long> create(@Valid @RequestBody TagSaveReq req) {
        return R.ok(commands.execute(MemberReqConverter.tag(null, req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改Tag")
    @RequiresPermissions("member:tag:update")
    public R<Long> update(@PathVariable Long id, @Valid @RequestBody TagSaveReq req) {
        return R.ok(commands.execute(MemberReqConverter.tag(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除未被会员使用的Tag")
    @RequiresPermissions("member:tag:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(commands.execute(new TagDeleteCmd(id)));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "Tag简洁列表")
    @RequiresPermissions("member:tag:query")
    public R<List<TagRes>> simple() {
        return R.ok(queries.execute(new TagsQry()).stream().map(MemberResConverter::tag).toList());
    }
}
