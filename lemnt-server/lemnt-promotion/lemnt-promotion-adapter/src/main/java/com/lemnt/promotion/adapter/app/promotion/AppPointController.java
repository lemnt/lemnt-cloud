package com.lemnt.promotion.adapter.app.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.promotion.adapter.app.promotion.converter.AppPointConverter;
import com.lemnt.promotion.adapter.app.promotion.res.AppPointActivityRes;
import com.lemnt.promotion.application.promotion.executor.PointAppDetailQueryExe;
import com.lemnt.promotion.application.promotion.executor.PointAppPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;


/** 小程序积分商城入口。 */
@Tag(name = "小程序积分商城")
@RestController
@RequestMapping("/mall/app/point-mall")
@RequiredArgsConstructor
public class AppPointController {
    private final PointAppPageQueryExe page;
    private final PointAppDetailQueryExe detail;

    @GetMapping("/activities")
    @Operation(summary = "积分商城活动分页")
    public R<AppPointActivityRes> activities(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        var result = page.execute(pageNum, pageSize);
        return R.page(
                result.getRecords().stream().map(AppPointConverter::activity).toList(),
                result.getTotal());
    }

    @GetMapping("/activities/{id}")
    @Operation(summary = "积分商城活动详情")
    public R<AppPointActivityRes> activity(@PathVariable Long id) {
        return R.ok(AppPointConverter.activity(detail.execute(id)));
    }
}
