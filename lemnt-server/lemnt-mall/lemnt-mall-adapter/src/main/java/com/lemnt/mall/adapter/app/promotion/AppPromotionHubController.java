package com.lemnt.mall.adapter.app.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.mall.adapter.app.promotion.converter.AppPromotionHubConverter;
import com.lemnt.mall.adapter.app.promotion.res.AppDiscountActivityRes;
import com.lemnt.mall.adapter.app.promotion.res.AppPromotionSectionRes;
import com.lemnt.mall.adapter.app.promotion.res.AppRewardActivityRes;
import com.lemnt.mall.application.promotion.executor.AppDiscountDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.AppPromotionHubQueryExe;
import com.lemnt.mall.application.promotion.executor.AppRewardDetailQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序营销活动聚合与规则详情。 */
@Tag(name = "小程序活动聚合")
@RestController
@RequestMapping("/mall/app/promotion-hub")
@RequiredArgsConstructor
public class AppPromotionHubController {
    private final AppPromotionHubQueryExe sections;
    private final AppDiscountDetailQueryExe discount;
    private final AppRewardDetailQueryExe reward;

    @GetMapping("/sections")
    @Operation(summary = "当前进行中的营销活动聚合")
    public R<List<AppPromotionSectionRes>> sections(@RequestParam(required = false) Integer limit) {
        return R.ok(sections.execute(limit).stream().map(AppPromotionHubConverter::section).toList());
    }

    @GetMapping("/discount/{id}")
    @Operation(summary = "限时折扣活动详情")
    public R<AppDiscountActivityRes> discount(@PathVariable Long id) {
        return R.ok(AppPromotionHubConverter.discount(discount.execute(id)));
    }

    @GetMapping("/reward/{id}")
    @Operation(summary = "满减送活动详情")
    public R<AppRewardActivityRes> reward(@PathVariable Long id) {
        return R.ok(AppPromotionHubConverter.reward(reward.execute(id)));
    }
}
