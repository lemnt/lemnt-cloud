package com.lemnt.member.adapter.app.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.member.adapter.app.growth.converter.MemberGrowthReqConverter;
import com.lemnt.member.adapter.app.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.app.growth.req.PointRecordPageReq;
import com.lemnt.member.adapter.app.growth.res.AppMemberGrowthPointRes;
import com.lemnt.member.application.growth.executor.MemberAssetRecordPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 小程序积分流水接口。 */
@Tag(name = "小程序积分流水")
@RestController
@RequestMapping("/app/growths")
@RequiredArgsConstructor
public class AppMemberPointRecordController {

    private final MemberAssetRecordPageQueryExe memberAssetRecordPageQueryExe;

    @GetMapping("/point-records")
    @Operation(summary = "本人积分流水分页")
    public R<AppMemberGrowthPointRes> point(
            @Valid @ModelAttribute PointRecordPageReq req) {
        var page = memberAssetRecordPageQueryExe.execute(
                MemberGrowthReqConverter.toPointRecordPageQry(SecurityUtils.getUserId(), req));
        return R.page(
                page.getRecords().stream().map(MemberGrowthResConverter::toAppMemberGrowthPointRes).toList(),
                page.getTotal());
    }
}
