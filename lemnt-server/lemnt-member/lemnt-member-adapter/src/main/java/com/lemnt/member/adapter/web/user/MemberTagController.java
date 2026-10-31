package com.lemnt.member.adapter.web.user;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.user.converter.MemberUserReqConverter;
import com.lemnt.member.adapter.web.user.converter.MemberUserResConverter;
import com.lemnt.member.adapter.web.user.req.MemberTagCreateReq;
import com.lemnt.member.adapter.web.user.req.MemberTagUpdateReq;
import com.lemnt.member.adapter.web.user.res.MemberTagDetailRes;
import com.lemnt.member.adapter.web.user.res.MemberTagListRes;
import com.lemnt.member.adapter.web.user.res.MemberTagSimpleRes;
import com.lemnt.member.application.user.cmd.TagDeleteCmd;
import com.lemnt.member.application.user.executor.MemberTagDeleteCmdExe;
import com.lemnt.member.application.user.executor.MemberTagDetailQueryExe;
import com.lemnt.member.application.user.executor.MemberTagListQueryExe;
import com.lemnt.member.application.user.executor.MemberTagSaveCmdExe;
import com.lemnt.member.application.user.qry.TagDetailQry;
import com.lemnt.member.application.user.qry.TagListQry;

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
 * 后台会员标签接口。
 */
@Tag(name = "后台会员Tag")
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class MemberTagController {

    private final MemberTagDeleteCmdExe memberTagDeleteCmdExe;
    private final MemberTagDetailQueryExe memberTagDetailQueryExe;
    private final MemberTagSaveCmdExe memberTagSaveCmdExe;
    private final MemberTagListQueryExe memberTagListQueryExe;

    @GetMapping
    @Operation(summary = "Tag列表")
    @RequiresPermissions("member:tag:query")
    public R<List<MemberTagListRes>> list() {
        return R.ok(memberTagListQueryExe.execute(new TagListQry()).stream().map(MemberUserResConverter::toMemberTagListRes).toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Tag详情")
    @RequiresPermissions("member:tag:query")
    public R<MemberTagDetailRes> detail(@PathVariable Long id) {
        return R.ok(MemberUserResConverter.toMemberTagDetailRes(memberTagDetailQueryExe.execute(new TagDetailQry(id))));
    }

    @PostMapping
    @Operation(summary = "创建Tag")
    @RequiresPermissions("member:tag:create")
    public R<Long> create(@Valid @RequestBody MemberTagCreateReq req) {
        return R.ok(memberTagSaveCmdExe.execute(MemberUserReqConverter.tag(null, req)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改Tag")
    @RequiresPermissions("member:tag:update")
    public R<Long> update(@PathVariable Long id, @Valid @RequestBody MemberTagUpdateReq req) {
        return R.ok(memberTagSaveCmdExe.execute(MemberUserReqConverter.tag(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除未被会员使用的Tag")
    @RequiresPermissions("member:tag:delete")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(memberTagDeleteCmdExe.execute(new TagDeleteCmd(id)));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "Tag简洁列表")
    @RequiresPermissions("member:tag:query")
    public R<List<MemberTagSimpleRes>> simple() {
        return R.ok(memberTagListQueryExe.execute(new TagListQry()).stream().map(MemberUserResConverter::toMemberTagSimpleRes).toList());
    }
}
