package com.lemnt.member.adapter.web.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.web.growth.converter.MemberReqConverter;
import com.lemnt.member.adapter.web.growth.req.MemberRecordLevelRecordsReq;
import com.lemnt.member.adapter.web.growth.res.MemberRecordLevelRecordsDetailRes;
import com.lemnt.member.adapter.web.growth.res.MemberRecordLevelRecordsRes;
import com.lemnt.member.application.growth.executor.MemberLevelRecordAllPageQueryExe;
import com.lemnt.member.application.growth.executor.MemberLevelRecordDetailQueryExe;
import com.lemnt.member.application.growth.qry.LevelRecordDetailQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 等级流水接口。 */
@Tag(name = "等级流水")
@RestController
@RequestMapping("/member/admin")
@RequiredArgsConstructor
public class MemberLevelRecordController {

    private final MemberLevelRecordAllPageQueryExe memberLevelRecordAllPageQueryExe;
    private final MemberLevelRecordDetailQueryExe memberLevelRecordDetailQueryExe;

    @GetMapping("/level-records")
    @Operation(summary = "level-records分页")
    @RequiresPermissions("member:level:query")
    public R<MemberRecordLevelRecordsRes> levelRecords(
            @Valid @ModelAttribute MemberRecordLevelRecordsReq req) {
        var page = memberLevelRecordAllPageQueryExe.execute(MemberReqConverter.toLevelRecordPageQry(req));
        return R.page(
                page.getRecords().stream().map(MemberGrowthResConverter::toMemberRecordLevelRecordsRes).toList(),
                page.getTotal());
    }

    @GetMapping("/level-records/{id}")
    @Operation(summary = "level-records详情")
    @RequiresPermissions("member:level:query")
    public R<MemberRecordLevelRecordsDetailRes> levelRecordsDetail(@PathVariable Long id) {
        return R.ok(
                MemberGrowthResConverter.toMemberRecordLevelRecordsDetailRes(
                        memberLevelRecordDetailQueryExe.execute(new LevelRecordDetailQry(id))));
    }
}
