package com.lemnt.member.adapter.web.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthReqConverter;
import com.lemnt.member.adapter.web.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.web.growth.res.MemberUserSignsRes;
import com.lemnt.member.adapter.web.user.req.MemberUserSignsReq;
import com.lemnt.member.application.growth.executor.MemberSignRecordPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 会员签到记录接口。 */
@Tag(name = "会员签到记录")
@RestController
@RequestMapping("/member-users")
@RequiredArgsConstructor
public class MemberUserSignRecordController {

    private final MemberSignRecordPageQueryExe memberSignRecordPageQueryExe;

    @GetMapping("/{id}/signin-records")
    @Operation(summary = "会员签到记录分页")
    @RequiresPermissions("member:sign-in-record:query")
    public R<MemberUserSignsRes> signs(
            @PathVariable Long id, @Valid @ModelAttribute MemberUserSignsReq req) {
        var page = memberSignRecordPageQueryExe.execute(MemberGrowthReqConverter.signs(id, req));
        return R.page(
                page.getRecords().stream().map(MemberGrowthResConverter::toMemberUserSignsRes).toList(),
                page.getTotal());
    }
}
