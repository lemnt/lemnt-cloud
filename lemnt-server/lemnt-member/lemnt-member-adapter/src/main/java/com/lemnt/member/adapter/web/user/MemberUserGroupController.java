package com.lemnt.member.adapter.web.user;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.user.converter.MemberUserReqConverter;
import com.lemnt.member.adapter.web.user.req.GroupAssignReq;
import com.lemnt.member.application.user.executor.MemberGroupAssignCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 会员分组分配接口。 */
@Tag(name = "会员分组分配")
@RestController
@RequestMapping("/member-users")
@RequiredArgsConstructor
public class MemberUserGroupController {

    private final MemberGroupAssignCmdExe memberGroupAssignCmdExe;

    @PutMapping("/{id}/group")
    @Operation(summary = "分配或清空分组")
    @RequiresPermissions("member:user:update")
    public R<Boolean> group(@PathVariable Long id, @Valid @RequestBody GroupAssignReq req) {
        return R.ok(memberGroupAssignCmdExe.execute(MemberUserReqConverter.group(id, req)));
    }
}
