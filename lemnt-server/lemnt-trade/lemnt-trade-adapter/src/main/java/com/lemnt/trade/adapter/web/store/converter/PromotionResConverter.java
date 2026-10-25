package com.lemnt.trade.adapter.web.store.converter;

import com.lemnt.trade.application.store.dto.*;
import com.lemnt.trade.adapter.web.store.res.*;
import com.lemnt.trade.adapter.web.checkout.converter.CheckoutResConverter;

public final class PromotionResConverter {
    private PromotionResConverter() {}

    public static PromotionPageRes page(PromotionPageDTO p) {
        return new PromotionPageRes(
                p.records().stream()
                        .map(
                                v ->
                                        new CouponOfferRes(
                                                CheckoutResConverter.couponTemplate(v.template()),
                                                v.couponId(),
                                                v.couponStatus(),
                                                v.usable(),
                                                v.claimable()))
                        .toList(),
                p.total());
    }

    public static CartAdditionRes add(CartAdditionDTO v) {
        return new CartAdditionRes(
                v.requestNo(), v.productId(), v.addedQuantity(), v.resultingQuantity());
    }
}
