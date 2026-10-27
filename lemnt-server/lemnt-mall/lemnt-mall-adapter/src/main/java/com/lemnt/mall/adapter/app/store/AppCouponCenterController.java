package com.lemnt.mall.adapter.app.store;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.mall.adapter.app.checkout.converter.CheckoutResConverter;
import com.lemnt.mall.adapter.app.checkout.res.MallAppCouponCenterClaimRes;
import com.lemnt.mall.adapter.app.store.converter.PromotionReqConverter;
import com.lemnt.mall.adapter.app.store.converter.PromotionResConverter;
import com.lemnt.mall.adapter.app.store.req.MallAppCouponCenterOffersReq;
import com.lemnt.mall.adapter.app.store.req.MallAppCouponCenterWalletReq;
import com.lemnt.mall.adapter.app.store.res.MallAppCouponCenterOffersRes;
import com.lemnt.mall.adapter.app.store.res.MallAppCouponCenterWalletRes;
import com.lemnt.mall.application.store.cmd.CouponClaimCmd;
import com.lemnt.mall.application.store.executor.CouponClaimCmdExe;
import com.lemnt.mall.application.store.executor.CouponOffersPageQueryExe;
import com.lemnt.mall.application.store.executor.CouponWalletPageQueryExe;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

/** 小程序领券中心接口。 */
@Tag(name = "小程序领券中心")
@RestController
@RequestMapping("/mall/app")
@RequiredArgsConstructor
public class AppCouponCenterController {
    private final CouponClaimCmdExe couponClaimCmdExe;
    private final CouponOffersPageQueryExe couponOffersPageQueryExe;
    private final CouponWalletPageQueryExe couponWalletPageQueryExe;

    @GetMapping("/coupon-center")
    @Operation(summary = "本人可见的公开领券中心")
    public R<MallAppCouponCenterOffersRes> offers(
            @Valid @ModelAttribute MallAppCouponCenterOffersReq req) {
        var page = couponOffersPageQueryExe.execute(PromotionReqConverter.offers(SecurityUtils.getUserId(), req));
        return R.page(page.records().stream().map(PromotionResConverter::toMallAppCouponCenterOffersRes).toList(),
                page.total());
    }

    @GetMapping("/coupon-wallet")
    @Operation(summary = "本人优惠券及服务端当前可用状态")
    public R<MallAppCouponCenterWalletRes> wallet(
            @Valid @ModelAttribute MallAppCouponCenterWalletReq req) {
        var page = couponWalletPageQueryExe.execute(PromotionReqConverter.wallet(SecurityUtils.getUserId(), req));
        return R.page(page.records().stream().map(PromotionResConverter::toMallAppCouponCenterWalletRes).toList(),
                page.total());
    }

    @PostMapping("/coupon-center/{templateId}/claim")
    @Operation(summary = "本人领取公开优惠券，每模板限一张，重复返回原券")
    public R<MallAppCouponCenterClaimRes> claim(
            @PathVariable Long templateId) {
        return R.ok(
                CheckoutResConverter.couponToMallAppCouponCenterClaimRes(
                couponClaimCmdExe.execute(
                new CouponClaimCmd(SecurityUtils.getUserId(), templateId))));
    }
}
