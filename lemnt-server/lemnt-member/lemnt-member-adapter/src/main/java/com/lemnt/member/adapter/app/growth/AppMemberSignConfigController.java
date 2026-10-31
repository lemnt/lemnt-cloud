package com.lemnt.member.adapter.app.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.member.adapter.app.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.app.growth.res.SignConfigRes;
import com.lemnt.member.application.growth.executor.MemberSignConfigListQueryExe;
import com.lemnt.member.application.growth.qry.SignConfigListQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序签到规则接口。 */
@Tag(name = "小程序签到规则")
@RestController
@RequestMapping("/app/growths")
@RequiredArgsConstructor
public class AppMemberSignConfigController {

    private final MemberSignConfigListQueryExe memberSignConfigListQueryExe;

    @GetMapping("/signin/config")
    @Operation(summary = "已启用签到规则")
    public R<List<SignConfigRes>> signConfig() {
        return R.ok(
                memberSignConfigListQueryExe.execute(new SignConfigListQry(true)).stream()
                .map(MemberGrowthResConverter::signConfig)
                .toList());
    }
}
