package com.lemnt.member.adapter.web.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.web.growth.converter.MemberReqConverter;
import com.lemnt.member.adapter.web.growth.req.MemberRecordSigninRecordsReq;
import com.lemnt.member.adapter.web.growth.res.MemberRecordSigninRecordsRes;
import com.lemnt.member.application.growth.executor.MemberSignRecordAllPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 签到记录接口。 */
@Tag(name = "签到记录")
@RestController
@RequestMapping("/member/admin")
@RequiredArgsConstructor
public class MemberSignRecordController {

    private final MemberSignRecordAllPageQueryExe memberSignRecordAllPageQueryExe;

    @GetMapping("/signin-records")
    @Operation(summary = "signin-records分页")
    @RequiresPermissions("member:signin:query")
    public R<MemberRecordSigninRecordsRes> signinRecords(
            @Valid @ModelAttribute MemberRecordSigninRecordsReq req) {
        var page = memberSignRecordAllPageQueryExe.execute(MemberReqConverter.toSignRecordPageQry(req));
        return R.page(
                page.getRecords().stream().map(MemberGrowthResConverter::toMemberRecordSigninRecordsRes).toList(),
                page.getTotal());
    }
}
