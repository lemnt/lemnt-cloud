package com.lemnt.member.adapter.web.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthReqConverter;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.web.growth.req.MemberUserExperienceReq;
import com.lemnt.member.adapter.web.growth.res.MemberUserExperienceRecordsRes;
import com.lemnt.member.adapter.web.growth.res.MemberUserExperienceRes;
import com.lemnt.member.adapter.web.user.req.MemberUserExperienceRecordsReq;
import com.lemnt.member.application.growth.executor.MemberAssetAdjustCmdExe;
import com.lemnt.member.application.growth.executor.MemberAssetRecordPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 会员经验接口。 */
@Tag(name = "会员经验")
@RestController
@RequestMapping("/member-users")
@RequiredArgsConstructor
public class MemberUserExperienceController {

    private final MemberAssetAdjustCmdExe memberAssetAdjustCmdExe;
    private final MemberAssetRecordPageQueryExe memberAssetRecordPageQueryExe;

    @PutMapping("/{id}/experience")
    @Operation(summary = "幂等变更会员experience，余额不能为负")
    @RequiresPermissions("member:user:update-level")
    public R<MemberUserExperienceRes> experience(
            @PathVariable Long id, @Valid @RequestBody MemberUserExperienceReq req) {
        return R.ok(
                MemberGrowthResConverter.toMemberUserExperienceRes(
                        memberAssetAdjustCmdExe.execute(MemberGrowthReqConverter.asset(id, "EXPERIENCE", req))));
    }

    @GetMapping("/{id}/experience-records")
    @Operation(summary = "会员experience流水分页")
    @RequiresPermissions("member:experience-record:query")
    public R<MemberUserExperienceRecordsRes> experienceRecords(
            @PathVariable Long id, @Valid @ModelAttribute MemberUserExperienceRecordsReq req) {
        var page = memberAssetRecordPageQueryExe.execute(MemberGrowthReqConverter.assets(id, "EXPERIENCE", req));
        return R.page(
                page.getRecords().stream().map(MemberGrowthResConverter::toMemberUserExperienceRecordsRes).toList(),
                page.getTotal());
    }
}
