package com.lemnt.member.adapter.app.growth;

import com.lemnt.common.core.domain.R;
import com.lemnt.member.adapter.app.growth.converter.MemberGrowthResConverter;
import com.lemnt.member.adapter.app.growth.res.LevelRes;
import com.lemnt.member.application.growth.executor.MemberLevelListQueryExe;
import com.lemnt.member.application.growth.qry.LevelListQry;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序会员等级接口。 */
@Tag(name = "小程序会员等级")
@RestController
@RequestMapping("/app/growths")
@RequiredArgsConstructor
public class AppMemberLevelController {

    private final MemberLevelListQueryExe memberLevelListQueryExe;

    @GetMapping("/levels")
    @Operation(summary = "已启用会员等级")
    public R<List<LevelRes>> levels() {
        return R.ok(
                memberLevelListQueryExe.execute(new LevelListQry(true)).stream()
                .map(MemberGrowthResConverter::level)
                .toList());
    }
}
