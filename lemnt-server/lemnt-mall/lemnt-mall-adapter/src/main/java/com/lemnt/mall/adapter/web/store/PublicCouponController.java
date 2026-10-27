package com.lemnt.mall.adapter.web.store;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.store.converter.PromotionReqConverter;
import com.lemnt.mall.adapter.web.store.req.CouponPublicClaimReq;
import com.lemnt.mall.application.store.executor.CouponPublicClaimCmdExe;
import com.lemnt.mall.application.store.executor.CouponPublicTemplateListQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 后台优惠券公开领取设置。
 */
@Tag(name = "后台优惠券公开领取设置")
@RestController
@RequestMapping("/mall/admin/coupon-templates")
@RequiredArgsConstructor
public class PublicCouponController {
    private final CouponPublicTemplateListQueryExe couponPublicTemplateListQueryExe;
    private final CouponPublicClaimCmdExe couponPublicClaimCmdExe;

    @GetMapping("/public-claim")
    @RequiresPermissions("mall:coupon-template:list")
    @Operation(summary = "已开启公开领取的模板ID；不代表当前可领取")
    public R<List<Long>> settings() {
        return R.ok(couponPublicTemplateListQueryExe.execute());
    }

    @PutMapping("/{id}/public-claim")
    @RequiresPermissions("mall:coupon-template:edit")
    @Operation(summary = "开启或关闭会员自助领取；现有模板默认关闭")
    public R<Boolean> setting(
            @PathVariable Long id,
            @Valid @RequestBody CouponPublicClaimReq req) {
        return R.ok(couponPublicClaimCmdExe.execute(PromotionReqConverter.setting(id, req)));
    }
}
