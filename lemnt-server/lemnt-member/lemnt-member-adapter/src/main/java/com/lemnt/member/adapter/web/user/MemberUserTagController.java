package com.lemnt.member.adapter.web.user;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.user.converter.MemberUserReqConverter;
import com.lemnt.member.adapter.web.user.req.TagsAssignReq;
import com.lemnt.member.application.user.executor.MemberTagIdListQueryExe;
import com.lemnt.member.application.user.executor.MemberTagsAssignCmdExe;
import com.lemnt.member.application.user.qry.MemberTagIdListQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/** 会员标签关联接口。 */
@Tag(name = "会员标签关联")
@RestController
@RequestMapping("/member-users")
@RequiredArgsConstructor
public class MemberUserTagController {

    private final MemberTagsAssignCmdExe memberTagsAssignCmdExe;
    private final MemberTagIdListQueryExe memberTagIdListQueryExe;

    @GetMapping("/{id}/tags")
    @Operation(summary = "会员标签编号")
    @RequiresPermissions("member:user:query")
    public R<List<Long>> tags(@PathVariable Long id) {
        return R.ok(memberTagIdListQueryExe.execute(new MemberTagIdListQry(id)));
    }

    @PutMapping("/{id}/tags")
    @Operation(summary = "设置标签集合，空集合清空")
    @RequiresPermissions("member:user:update")
    public R<Boolean> tags(@PathVariable Long id, @Valid @RequestBody TagsAssignReq req) {
        return R.ok(memberTagsAssignCmdExe.execute(MemberUserReqConverter.tags(id, req)));
    }
}
