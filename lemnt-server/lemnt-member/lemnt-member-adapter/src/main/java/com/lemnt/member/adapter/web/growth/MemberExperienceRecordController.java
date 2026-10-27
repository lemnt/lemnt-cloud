package com.lemnt.member.adapter.web.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.web.growth.converter.MemberReqConverter;
import com.lemnt.member.adapter.web.growth.req.MemberRecordExperienceRecordsReq;
import com.lemnt.member.adapter.web.growth.res.MemberRecordExperienceRecordsDetailRes;
import com.lemnt.member.adapter.web.growth.res.MemberRecordExperienceRecordsRes;
import com.lemnt.member.application.growth.executor.MemberAssetRecordAllPageQueryExe;
import com.lemnt.member.application.growth.executor.MemberAssetRecordDetailQueryExe;
import com.lemnt.member.application.growth.qry.AssetRecordDetailQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 经验流水接口。 */
@Tag(name = "经验流水")
@RestController
@RequestMapping("/member/admin")
@RequiredArgsConstructor
public class MemberExperienceRecordController {

    private final MemberAssetRecordAllPageQueryExe memberAssetRecordAllPageQueryExe;
    private final MemberAssetRecordDetailQueryExe memberAssetRecordDetailQueryExe;

    @GetMapping("/experience-records")
    @Operation(summary = "experience-records分页")
    @RequiresPermissions("member:experience:query")
    public R<MemberRecordExperienceRecordsRes> experienceRecords(
            @Valid @ModelAttribute MemberRecordExperienceRecordsReq req) {
        var page = memberAssetRecordAllPageQueryExe.execute(MemberReqConverter.toPageQry("EXPERIENCE", req));
        return R.page(
                page.getRecords().stream().map(MemberGrowthResConverter::toMemberRecordExperienceRecordsRes).toList(),
                page.getTotal());
    }

    @GetMapping("/experience-records/{id}")
    @Operation(summary = "experience-records详情")
    @RequiresPermissions("member:experience:query")
    public R<MemberRecordExperienceRecordsDetailRes> experienceRecordsDetail(@PathVariable Long id) {
        return R.ok(
                MemberGrowthResConverter.toMemberRecordExperienceRecordsDetailRes(
                        memberAssetRecordDetailQueryExe.execute(new AssetRecordDetailQry("EXPERIENCE", id))));
    }
}
