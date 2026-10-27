package com.lemnt.member.adapter.app.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.member.adapter.app.growth.converter.MemberGrowthReqConverter;
import com.lemnt.member.adapter.app.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.app.growth.req.SignRecordPageReq;
import com.lemnt.member.adapter.app.growth.res.AppMemberGrowthSignRes;
import com.lemnt.member.adapter.app.growth.res.AppMemberGrowthSignsRes;
import com.lemnt.member.adapter.app.growth.res.SignSummaryRes;
import com.lemnt.member.application.growth.cmd.SignCreateCmd;
import com.lemnt.member.application.growth.executor.MemberSignCreateCmdExe;
import com.lemnt.member.application.growth.executor.MemberSignRecordPageQueryExe;
import com.lemnt.member.application.growth.executor.MemberSignSummaryQueryExe;
import com.lemnt.member.application.growth.qry.SignSummaryQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 小程序会员签到接口。 */
@Tag(name = "小程序会员签到")
@RestController
@RequestMapping("/app/growths")
@RequiredArgsConstructor
public class AppMemberSignRecordController {

    private final MemberSignCreateCmdExe memberSignCreateCmdExe;
    private final MemberSignSummaryQueryExe memberSignSummaryQueryExe;
    private final MemberSignRecordPageQueryExe memberSignRecordPageQueryExe;

    @GetMapping("/signin/summary")
    @Operation(summary = "本人签到汇总")
    public R<SignSummaryRes> summary() {
        return R.ok(
                MemberGrowthResConverter.summary(
                        memberSignSummaryQueryExe.execute(new SignSummaryQry(SecurityUtils.getUserId()))));
    }

    @PostMapping("/signin")
    @Operation(summary = "数据库日期每日幂等签到，积分与经验同事务发放")
    public R<AppMemberGrowthSignRes> sign() {
        return R.ok(
                MemberGrowthResConverter.toAppMemberGrowthSignRes(
                        memberSignCreateCmdExe.execute(new SignCreateCmd(SecurityUtils.getUserId()))));
    }

    @GetMapping("/signin/records")
    @Operation(summary = "本人签到记录分页")
    public R<AppMemberGrowthSignsRes> signs(
            @Valid @ModelAttribute SignRecordPageReq req) {
        var page = memberSignRecordPageQueryExe.execute(MemberGrowthReqConverter.toSignRecordPageQry(SecurityUtils.getUserId(), req));
        return R.page(
                page.getRecords().stream().map(MemberGrowthResConverter::toAppMemberGrowthSignsRes).toList(),
                page.getTotal());
    }
}
