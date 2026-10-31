package com.lemnt.mall.adapter.app.promotion;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import com.lemnt.mall.application.promotion.executor.PromotionOrderPlaceCmdExe;
import com.lemnt.mall.adapter.app.promotion.res.AppPromotionOrderRes;
import com.lemnt.mall.adapter.app.promotion.req.AppPromotionOrderCreateReq;
import com.lemnt.mall.adapter.app.promotion.converter.AppPromotionOrderConverter;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.common.core.domain.R;
import com.lemnt.mall.adapter.app.promotion.converter.AppSeckillConverter;
import com.lemnt.mall.adapter.app.promotion.req.AppSeckillPageReq;
import com.lemnt.mall.adapter.app.promotion.res.AppSeckillActivityRes;
import com.lemnt.mall.adapter.app.promotion.res.AppSeckillConfigRes;
import com.lemnt.mall.adapter.app.promotion.res.AppSeckillDetailRes;
import com.lemnt.mall.adapter.app.promotion.res.AppSeckillNowRes;
import com.lemnt.mall.application.promotion.executor.SeckillAppConfigListQueryExe;
import com.lemnt.mall.application.promotion.executor.SeckillAppDetailQueryExe;
import com.lemnt.mall.application.promotion.executor.SeckillAppPageQueryExe;
import com.lemnt.mall.application.promotion.executor.SeckillNowQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/** 小程序秒杀展示入口。 */
@Tag(name = "小程序秒杀")
@RestController
@RequestMapping("/mall/app/seckill")
@RequiredArgsConstructor
public class AppSeckillController {
    private final SeckillAppConfigListQueryExe configs;
    private final SeckillNowQueryExe now;
    private final SeckillAppPageQueryExe page;
    private final PromotionOrderPlaceCmdExe orderPlace;
    private final SeckillAppDetailQueryExe detail;

    @GetMapping("/configs")
    @Operation(summary = "开启状态的秒杀时段")
    public R<List<AppSeckillConfigRes>> configs() {
        return R.ok(configs.execute().stream().map(AppSeckillConverter::config).toList());
    }

    @GetMapping("/now")
    @Operation(summary = "当前秒杀时段及活动")
    public R<AppSeckillNowRes> now() {
        return R.ok(AppSeckillConverter.now(now.execute()));
    }

    @GetMapping("/activities")
    @Operation(summary = "秒杀活动分页")
    public R<AppSeckillActivityRes> page(@Valid @ModelAttribute AppSeckillPageReq req) {
        var result = page.execute(AppSeckillConverter.page(req));
        return R.page(result.getRecords().stream().map(AppSeckillConverter::activity).toList(),
                result.getTotal());
    }

    @GetMapping("/activities/{id}")
    @Operation(summary = "秒杀活动详情")
    public R<AppSeckillDetailRes> detail(@PathVariable Long id) {
        return R.ok(AppSeckillConverter.detail(detail.execute(id)));
    }

    @PostMapping("/orders")
    @Operation(summary = "秒杀活动下单")
    public R<AppPromotionOrderRes> order(@Valid @RequestBody AppPromotionOrderCreateReq req) {
        return R.ok(AppPromotionOrderConverter.res(orderPlace.execute(
                AppPromotionOrderConverter.cmd(SecurityUtils.getUserId(), "SECKILL", req))));
    }
}
