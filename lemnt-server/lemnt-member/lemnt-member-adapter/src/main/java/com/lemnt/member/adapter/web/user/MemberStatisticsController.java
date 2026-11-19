package com.lemnt.member.adapter.web.user;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.member.adapter.web.user.converter.MemberStatisticsConverter;
import com.lemnt.member.adapter.web.user.req.MemberStatisticsReq;
import com.lemnt.member.adapter.web.user.res.MemberStatisticsRes;
import com.lemnt.member.application.user.executor.MemberStatisticsQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 后台会员统计。 */
@Tag(name = "会员统计")
@RestController
@RequestMapping("/member-users/statistics")
@RequiredArgsConstructor
public class MemberStatisticsController {
    private final MemberStatisticsQueryExe query;

    /** 查询日新增注册人数及当前会员档案分布。 */
    @GetMapping
    @Operation(summary = "会员注册趋势和档案分布")
    @RequiresPermissions("member:user:query")
    public R<MemberStatisticsRes> statistics(@Valid @ModelAttribute MemberStatisticsReq req) {
        return R.ok(
                MemberStatisticsConverter.toRes(
                        query.execute(MemberStatisticsConverter.toQry(req))));
    }
}
