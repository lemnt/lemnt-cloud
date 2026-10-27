package com.lemnt.member.adapter.web.user;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.user.converter.MemberUserReqConverter;
import com.lemnt.member.adapter.web.user.converter.MemberUserResConverter;
import com.lemnt.member.adapter.web.user.req.MemberGroupCreateReq;
import com.lemnt.member.adapter.web.user.req.MemberGroupUpdateReq;
import com.lemnt.member.adapter.web.user.res.MemberGroupDetailRes;
import com.lemnt.member.adapter.web.user.res.MemberGroupListRes;
import com.lemnt.member.adapter.web.user.res.MemberGroupSimpleRes;
import com.lemnt.member.application.user.cmd.GroupDeleteCmd;
import com.lemnt.member.application.user.executor.MemberGroupDeleteCmdExe;
import com.lemnt.member.application.user.executor.MemberGroupDetailQueryExe;
import com.lemnt.member.application.user.executor.MemberGroupListQueryExe;
import com.lemnt.member.application.user.executor.MemberGroupSaveCmdExe;
import com.lemnt.member.application.user.qry.GroupDetailQry;
import com.lemnt.member.application.user.qry.GroupListQry;

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
import java.util.List;

/**
 * 后台会员分组接口。
 */
@Tag(name = "后台会员Group")
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class MemberGroupController {

    private final MemberGroupDeleteCmdExe memberGroupDeleteCmdExe;
    private final MemberGroupDetailQueryExe memberGroupDetailQueryExe;
    private final MemberGroupSaveCmdExe memberGroupSaveCmdExe;
    private final MemberGroupListQueryExe memberGroupListQueryExe;

    @GetMapping
    @Operation(summary = "Group列表")
    @RequiresPermissions("member:group:query")
    public R<List<MemberGroupListRes>> list() {
        return R.ok(
                memberGroupListQueryExe.execute(new GroupListQry()).stream().map(MemberUserResConverter::toMemberGroupListRes).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Group详情")
    @RequiresPermissions("member:group:query")
    public R<MemberGroupDetailRes> detail(@PathVariable Long id) {
        return R.ok(MemberUserResConverter.toMemberGroupDetailRes(memberGroupDetailQueryExe.execute(new GroupDetailQry(id))));
    }

    @PostMapping
    @Operation(summary = "创建Group")
    @RequiresPermissions("member:group:create")
    public R<Long> create(@Valid @RequestBody MemberGroupCreateReq req) {
        return R.ok(memberGroupSaveCmdExe.execute(MemberUserReqConverter.group(null, req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改Group")
    @RequiresPermissions("member:group:update")
    public R<Long> update(@PathVariable Long id, @Valid @RequestBody MemberGroupUpdateReq req) {
        return R.ok(memberGroupSaveCmdExe.execute(MemberUserReqConverter.group(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除未被会员使用的Group")
    @RequiresPermissions("member:group:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(memberGroupDeleteCmdExe.execute(new GroupDeleteCmd(id)));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "Group简洁列表")
    @RequiresPermissions("member:group:query")
    public R<List<MemberGroupSimpleRes>> simple() {
        return R.ok(
                memberGroupListQueryExe.execute(new GroupListQry()).stream()
                .filter(v -> v.status() == 1)
                .map(MemberUserResConverter::toMemberGroupSimpleRes)
                .toList());
    }
}
