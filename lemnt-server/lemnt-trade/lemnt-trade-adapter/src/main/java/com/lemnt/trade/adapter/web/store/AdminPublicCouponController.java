package com.lemnt.trade.adapter.web.store;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.trade.application.store.executor.*;
import com.lemnt.trade.adapter.web.store.req.CouponPublicClaimReq;
import com.lemnt.trade.adapter.web.store.converter.PromotionReqConverter;
import org.springframework.web.bind.annotation.*;

@io.swagger.v3.oas.annotations.tags.Tag(name = "后台优惠券公开领取设置")
@RestController
@RequestMapping("/trade/admin/coupon-templates")
@lombok.RequiredArgsConstructor
public class AdminPublicCouponController {
    private final PromotionQueryExe queries;
    private final StorefrontCmdExe commands;

    @GetMapping("/public-claim")
    @RequiresPermissions("trade:coupon-template:list")
    @io.swagger.v3.oas.annotations.Operation(summary = "已开启公开领取的模板ID；不代表当前可领取")
    public R<java.util.List<Long>> settings() {
        return R.ok(queries.execute());
    }

    @PutMapping("/{id}/public-claim")
    @RequiresPermissions("trade:coupon-template:edit")
    @io.swagger.v3.oas.annotations.Operation(summary = "开启或关闭会员自助领取；现有模板默认关闭")
    public R<Boolean> setting(
            @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody CouponPublicClaimReq req) {
        return R.ok(commands.execute(PromotionReqConverter.setting(id, req)));
    }
}
