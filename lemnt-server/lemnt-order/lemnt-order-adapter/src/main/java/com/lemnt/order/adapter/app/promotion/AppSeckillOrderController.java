package com.lemnt.order.adapter.app.promotion;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.order.adapter.app.promotion.converter.AppPromotionOrderConverter;
import com.lemnt.order.adapter.app.promotion.converter.AppSeckillConverter;
import com.lemnt.order.adapter.app.promotion.req.AppPromotionOrderCreateReq;
import com.lemnt.order.adapter.app.promotion.req.AppSeckillPageReq;
import com.lemnt.order.adapter.app.promotion.res.AppPromotionOrderRes;
import com.lemnt.order.adapter.app.promotion.res.AppSeckillActivityRes;
import com.lemnt.order.adapter.app.promotion.res.AppSeckillConfigRes;
import com.lemnt.order.adapter.app.promotion.res.AppSeckillDetailRes;
import com.lemnt.order.adapter.app.promotion.res.AppSeckillNowRes;
import com.lemnt.order.application.promotion.executor.PromotionOrderPlaceCmdExe;
import com.lemnt.order.application.promotion.executor.SeckillAppConfigListQueryExe;
import com.lemnt.order.application.promotion.executor.SeckillAppDetailQueryExe;
import com.lemnt.order.application.promotion.executor.SeckillAppPageQueryExe;
import com.lemnt.order.application.promotion.executor.SeckillNowQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/** 原活动下单入口，统一由订单服务执行。 */
@Tag(name="活动订单")
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
