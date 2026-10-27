package com.lemnt.member.adapter.web.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthReqConverter;
import com.lemnt.member.adapter.web.growth.req.LevelAssignReq;
import com.lemnt.member.application.growth.executor.MemberLevelAssignCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 会员等级分配接口。 */
@Tag(name = "会员等级分配")
@RestController
@RequestMapping("/member-users")
@RequiredArgsConstructor
public class MemberUserLevelController {

    private final MemberLevelAssignCmdExe memberLevelAssignCmdExe;

    @PutMapping("/{id}/level")
    @Operation(summary = "调整等级及对应经验，幂等业务编号")
    @RequiresPermissions("member:user:update-level")
    public R<Boolean> level(@PathVariable Long id, @Valid @RequestBody LevelAssignReq req) {
        return R.ok(memberLevelAssignCmdExe.execute(MemberGrowthReqConverter.level(id, req)));
    }
}
