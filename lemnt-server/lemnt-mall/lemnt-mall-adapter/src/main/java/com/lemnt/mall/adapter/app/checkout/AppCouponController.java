package com.lemnt.mall.adapter.app.checkout;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.mall.adapter.app.checkout.converter.CheckoutResConverter;
import com.lemnt.mall.adapter.app.checkout.res.MallAppCouponCouponsRes;
import com.lemnt.mall.application.checkout.executor.CouponListQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序持有优惠券接口。 */
@Tag(name = "小程序持有优惠券")
@RestController
@RequestMapping("/mall/app")
@RequiredArgsConstructor
public class AppCouponController {
    private final CouponListQueryExe couponListQueryExe;

    @GetMapping("/coupons")
    @Operation(summary = "当前会员持有的优惠券")
    public R<List<MallAppCouponCouponsRes>> coupons() {
        Long memberId = SecurityUtils.getUserId();
        return R.ok(couponListQueryExe.execute(memberId).stream().map(CheckoutResConverter::couponToMallAppCouponCouponsRes).toList());
    }
}
