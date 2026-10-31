package com.lemnt.member.adapter.web.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthReqConverter;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.web.growth.req.MemberUserPointsReq;
import com.lemnt.member.adapter.web.growth.res.MemberUserPointsRecordsRes;
import com.lemnt.member.adapter.web.growth.res.MemberUserPointsRes;
import com.lemnt.member.adapter.web.user.req.MemberUserPointsRecordsReq;
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

/** 会员积分接口。 */
@Tag(name = "会员积分")
@RestController
@RequestMapping("/member-users")
@RequiredArgsConstructor
public class MemberUserPointController {

    private final MemberAssetAdjustCmdExe memberAssetAdjustCmdExe;
    private final MemberAssetRecordPageQueryExe memberAssetRecordPageQueryExe;

    @PutMapping("/{id}/points")
    @Operation(summary = "幂等变更会员points，余额不能为负")
    @RequiresPermissions("member:user:update-point")
    public R<MemberUserPointsRes> points(@PathVariable Long id, @Valid @RequestBody MemberUserPointsReq req) {
        return R.ok(
                MemberGrowthResConverter.toMemberUserPointsRes(
                        memberAssetAdjustCmdExe.execute(MemberGrowthReqConverter.asset(id, "POINT", req))));
    }

    @GetMapping("/{id}/points-records")
    @Operation(summary = "会员points流水分页")
    @RequiresPermissions("member:point-record:query")
    public R<MemberUserPointsRecordsRes> pointsRecords(
            @PathVariable Long id, @Valid @ModelAttribute MemberUserPointsRecordsReq req) {
        var page = memberAssetRecordPageQueryExe.execute(MemberGrowthReqConverter.assets(id, "POINT", req));
        return R.page(
                page.getRecords().stream().map(MemberGrowthResConverter::toMemberUserPointsRecordsRes).toList(),
                page.getTotal());
    }
}
