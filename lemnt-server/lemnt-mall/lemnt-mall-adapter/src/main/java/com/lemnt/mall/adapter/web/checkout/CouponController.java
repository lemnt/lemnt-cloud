package com.lemnt.mall.adapter.web.checkout;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.checkout.converter.CheckoutResConverter;
import com.lemnt.mall.adapter.web.checkout.res.MallCouponCouponsRes;
import com.lemnt.mall.adapter.web.checkout.res.MallCouponIssueRes;
import com.lemnt.mall.application.checkout.cmd.CouponIssueCmd;
import com.lemnt.mall.application.checkout.executor.CouponIssueCmdExe;
import com.lemnt.mall.application.checkout.executor.CouponListQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 会员优惠券接口。 */
@Tag(name = "会员优惠券")
@RestController
@RequestMapping("/mall/admin")
@RequiredArgsConstructor
public class CouponController {
    private final CouponIssueCmdExe couponIssueCmdExe;
    private final CouponListQueryExe couponListQueryExe;

    @Operation(summary = "为目标会员发券，每人每模板限一张，同次重试幂等")
    @PostMapping("/members/{memberId}/coupons/{templateId}")
    @RequiresPermissions("mall:coupon:issue")
    public R<MallCouponIssueRes> issue(@PathVariable Long memberId, @PathVariable Long templateId) {
        return R.ok(
                CheckoutResConverter.couponToMallCouponIssueRes(
                couponIssueCmdExe.execute(new CouponIssueCmd(memberId, templateId))));
    }

    @Operation(summary = "查看目标会员优惠券，超过1000条时拒绝截断")
    @GetMapping("/members/{memberId}/coupons")
    @RequiresPermissions("mall:coupon:list")
    public R<List<MallCouponCouponsRes>> coupons(@PathVariable Long memberId) {
        return R.ok(couponListQueryExe.execute(memberId).stream().map(CheckoutResConverter::couponToMallCouponCouponsRes).toList());
    }
}
