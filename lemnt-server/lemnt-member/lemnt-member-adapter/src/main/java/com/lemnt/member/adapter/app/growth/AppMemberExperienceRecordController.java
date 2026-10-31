package com.lemnt.member.adapter.app.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.member.adapter.app.growth.converter.MemberGrowthReqConverter;
import com.lemnt.member.adapter.app.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.app.growth.req.ExperienceRecordPageReq;
import com.lemnt.member.adapter.app.growth.res.AppMemberGrowthExperienceRes;
import com.lemnt.member.application.growth.executor.MemberAssetRecordPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 小程序经验流水接口。 */
@Tag(name = "小程序经验流水")
@RestController
@RequestMapping("/app/growths")
@RequiredArgsConstructor
public class AppMemberExperienceRecordController {

    private final MemberAssetRecordPageQueryExe memberAssetRecordPageQueryExe;

    @GetMapping("/experience-records")
    @Operation(summary = "本人经验流水分页")
    public R<AppMemberGrowthExperienceRes> experience(
            @Valid @ModelAttribute ExperienceRecordPageReq req) {
        var page = memberAssetRecordPageQueryExe.execute(
                MemberGrowthReqConverter.toExperienceRecordPageQry(SecurityUtils.getUserId(), req));
        return R.page(
                page.getRecords().stream().map(MemberGrowthResConverter::toAppMemberGrowthExperienceRes).toList(),
                page.getTotal());
    }
}
