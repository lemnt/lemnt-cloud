package com.lemnt.order.adapter.app.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.order.adapter.app.promotion.converter.AppPromotionOrderConverter;
import com.lemnt.order.adapter.app.promotion.req.AppPromotionOrderCreateReq;
import com.lemnt.order.adapter.app.promotion.res.AppPromotionOrderRes;
import com.lemnt.order.application.promotion.executor.PromotionOrderPlaceCmdExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 原活动下单入口，统一由订单服务执行。 */
@Tag(name = "活动订单")
@RestController
@RequestMapping("/mall/app/seckill")
@RequiredArgsConstructor
public class AppSeckillOrderController {
    private final PromotionOrderPlaceCmdExe orderPlace;

    @PostMapping("/orders")
    @Operation(summary = "秒杀活动下单")
    public R<AppPromotionOrderRes> order(@Valid @RequestBody AppPromotionOrderCreateReq req) {
        return R.ok(
                AppPromotionOrderConverter.res(
                        orderPlace.execute(
                                AppPromotionOrderConverter.cmd(
                                        SecurityUtils.getUserId(), "SECKILL", req))));
    }
}
