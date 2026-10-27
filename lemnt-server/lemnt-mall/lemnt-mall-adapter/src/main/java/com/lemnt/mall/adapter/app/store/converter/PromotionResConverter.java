package com.lemnt.mall.adapter.app.store.converter;

import com.lemnt.mall.adapter.app.checkout.converter.CheckoutResConverter;
import com.lemnt.mall.adapter.app.store.res.CartAdditionRes;
import com.lemnt.mall.adapter.app.store.res.MallAppCouponCenterOffersRes;
import com.lemnt.mall.adapter.app.store.res.MallAppCouponCenterWalletRes;
import com.lemnt.mall.application.store.dto.CartAdditionDTO;
import com.lemnt.mall.application.store.dto.CouponOfferDTO;

/**
 * 营销响应转换器。
 */
public final class PromotionResConverter {
    private PromotionResConverter() {}

    /**
     * 新增。
     */
    public static CartAdditionRes add(CartAdditionDTO v) {
        return new CartAdditionRes(
                v.requestNo(), v.productId(), v.addedQuantity(), v.resultingQuantity());
    }

    /** 转换当前接口的优惠券分页项。 */
    public static MallAppCouponCenterOffersRes toMallAppCouponCenterOffersRes(CouponOfferDTO value) {
        return new MallAppCouponCenterOffersRes(CheckoutResConverter.couponTemplate(value.template()),
                value.couponId(),
                value.couponStatus(),
                value.usable(),
                value.claimable());
    }
    /** 转换当前接口的优惠券分页项。 */
    public static MallAppCouponCenterWalletRes toMallAppCouponCenterWalletRes(CouponOfferDTO value) {
        return new MallAppCouponCenterWalletRes(CheckoutResConverter.couponTemplate(value.template()),
                value.couponId(),
                value.couponStatus(),
                value.usable(),
                value.claimable());
    }
}
