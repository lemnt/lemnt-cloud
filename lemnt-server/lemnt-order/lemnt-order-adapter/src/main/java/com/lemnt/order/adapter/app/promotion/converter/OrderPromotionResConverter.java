package com.lemnt.order.adapter.app.promotion.converter;

import com.lemnt.order.adapter.app.promotion.res.*;
import com.lemnt.order.application.promotion.dto.*;

/** 优惠应用结果转HTTP响应，金额单位保持元。 */
public final class OrderPromotionResConverter {
    private OrderPromotionResConverter() {}

    /** 完整传递已冻结结果及历史未知状态。 */
    public static AppOrderPromotionDetailRes toRes(OrderPromotionDetailDTO value) {
        return new AppOrderPromotionDetailRes(
                value.orderNo(),
                value.snapshotStatus(),
                new AppOrderPromotionDetailRes.Summary(
                        value.summary().originalAmount(),
                        value.summary().discountAmount(),
                        value.summary().deliveryOriginalPrice(),
                        value.summary().deliveryDiscountPrice(),
                        value.summary().adjustmentAmount(),
                        value.summary().payAmount()),
                value.promotions().stream()
                        .map(
                                p ->
                                        new AppOrderPromotionDetailRes.Promotion(
                                                p.promotionType(),
                                                p.promotionName(),
                                                p.discountAmount(),
                                                p.deliveryDiscountPrice(),
                                                p.usePoint(),
                                                p.givePoint(),
                                                p.giveCouponTemplateCounts(),
                                                p.items().stream()
                                                        .map(
                                                                i ->
                                                                        new AppOrderPromotionDetailRes
                                                                                .Item(
                                                                                i.orderItemId(),
                                                                                i.discountAmount(),
                                                                                i
                                                                                        .deliveryDiscountPrice(),
                                                                                i.usePoint(),
                                                                                i.givePoint()))
                                                        .toList()))
                        .toList());
    }

    /** 完整传递已冻结结果及历史未知状态。 */
    public static AppAfterSalePromotionDetailRes toRes(AfterSalePromotionDetailDTO value) {
        return new AppAfterSalePromotionDetailRes(
                value.afterSaleNo(),
                value.orderItemId(),
                value.quantity(),
                value.amount(),
                value.discountAmount(),
                value.deliveryDiscountPrice(),
                value.returnUsePoint(),
                value.deductGivePoint(),
                value.ruleVersion(),
                value.snapshotStatus(),
                value.benefitStatus(),
                value.promotions().stream()
                        .map(
                                p ->
                                        new AppAfterSalePromotionDetailRes.Promotion(
                                                p.promotionType(),
                                                p.promotionName(),
                                                p.discountAmount(),
                                                p.deliveryDiscountPrice(),
                                                p.returnUsePoint(),
                                                p.deductGivePoint()))
                        .toList());
    }
}
