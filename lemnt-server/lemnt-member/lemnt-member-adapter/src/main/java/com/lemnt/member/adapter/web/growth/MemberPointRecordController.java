package com.lemnt.member.adapter.web.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.web.growth.converter.MemberReqConverter;
import com.lemnt.member.adapter.web.growth.req.MemberRecordPointRecordsReq;
import com.lemnt.member.adapter.web.growth.res.MemberRecordPointRecordsDetailRes;
import com.lemnt.member.adapter.web.growth.res.MemberRecordPointRecordsRes;
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

/** 积分流水接口。 */
@Tag(name = "积分流水")
@RestController
@RequestMapping("/member/admin")
@RequiredArgsConstructor
public class MemberPointRecordController {

    private final MemberAssetRecordAllPageQueryExe memberAssetRecordAllPageQueryExe;
    private final MemberAssetRecordDetailQueryExe memberAssetRecordDetailQueryExe;

    @GetMapping("/point-records")
    @Operation(summary = "point-records分页")
    @RequiresPermissions("member:point:query")
    public R<MemberRecordPointRecordsRes> pointRecords(
            @Valid @ModelAttribute MemberRecordPointRecordsReq req) {
        var page = memberAssetRecordAllPageQueryExe.execute(MemberReqConverter.toPageQry("POINT", req));
        return R.page(
                page.getRecords().stream().map(MemberGrowthResConverter::toMemberRecordPointRecordsRes).toList(),
                page.getTotal());
    }

    @GetMapping("/point-records/{id}")
    @Operation(summary = "point-records详情")
    @RequiresPermissions("member:point:query")
    public R<MemberRecordPointRecordsDetailRes> pointRecordsDetail(@PathVariable Long id) {
        return R.ok(
                MemberGrowthResConverter.toMemberRecordPointRecordsDetailRes(
                        memberAssetRecordDetailQueryExe.execute(new AssetRecordDetailQry("POINT", id))));
    }
}
