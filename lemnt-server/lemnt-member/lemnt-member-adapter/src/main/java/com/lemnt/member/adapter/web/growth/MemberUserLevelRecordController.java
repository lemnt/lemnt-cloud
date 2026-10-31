package com.lemnt.member.adapter.web.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthReqConverter;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.web.growth.res.MemberUserLevelRecordsRes;
import com.lemnt.member.adapter.web.user.req.MemberUserLevelRecordsReq;
import com.lemnt.member.application.growth.executor.MemberLevelRecordsPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 会员等级记录接口。 */
@Tag(name = "会员等级记录")
@RestController
@RequestMapping("/member-users")
@RequiredArgsConstructor
public class MemberUserLevelRecordController {

    private final MemberLevelRecordsPageQueryExe memberLevelRecordsPageQueryExe;

    @GetMapping("/{id}/level-records")
    @Operation(summary = "等级变更记录分页")
    @RequiresPermissions("member:level-record:query")
    public R<MemberUserLevelRecordsRes> levelRecords(
            @PathVariable Long id, @Valid @ModelAttribute MemberUserLevelRecordsReq req) {
        var page = memberLevelRecordsPageQueryExe.execute(MemberGrowthReqConverter.levelRecords(id, req));
        return R.page(
                page.getRecords().stream().map(MemberGrowthResConverter::toMemberUserLevelRecordsRes).toList(),
                page.getTotal());
    }
}
