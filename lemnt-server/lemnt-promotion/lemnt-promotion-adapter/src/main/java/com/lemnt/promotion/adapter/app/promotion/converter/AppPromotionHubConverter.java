package com.lemnt.promotion.adapter.app.promotion.converter;

import com.lemnt.promotion.adapter.app.promotion.res.AppDiscountActivityRes;
import com.lemnt.promotion.adapter.app.promotion.res.AppPromotionSectionRes;
import com.lemnt.promotion.adapter.app.promotion.res.AppRewardActivityRes;
import com.lemnt.promotion.application.promotion.dto.AppDiscountActivityDTO;
import com.lemnt.promotion.application.promotion.dto.AppPromotionSectionDTO;
import com.lemnt.promotion.application.promotion.dto.AppRewardActivityDTO;

/**
 * 小程序活动聚合及规则详情转换。
 */
public final class AppPromotionHubConverter {
    private AppPromotionHubConverter() {
    }

    public static AppPromotionSectionRes section(AppPromotionSectionDTO value) {
        return new AppPromotionSectionRes(
            value.type(),
            value.label(),
            value.items().stream()
                .map(
                    item ->
                        new AppPromotionSectionRes.Item(
                            item.id(),
                            item.name(),
                            item.startTime(),
                            item.endTime()))
                .toList());
    }

    public static AppDiscountActivityRes discount(AppDiscountActivityDTO value) {
        return new AppDiscountActivityRes(
            value.id(),
            value.name(),
            value.startTime(),
            value.endTime(),
            value.products().stream()
                .map(
                    item ->
                        new AppDiscountActivityRes.Product(
                            item.spuId(),
                            item.skuId(),
                            item.discountType(),
                            item.discountPercent(),
                            item.discountPrice()))
                .toList());
    }

    public static AppRewardActivityRes reward(AppRewardActivityDTO value) {
        return new AppRewardActivityRes(
            value.id(),
            value.name(),
            value.startTime(),
            value.endTime(),
            value.conditionType(),
            value.productScope(),
            value.rules().stream()
                .map(
                    item ->
                        new AppRewardActivityRes.Rule(
                            item.limit(),
                            item.discountPrice(),
                            item.freeDelivery(),
                            item.point(),
                            item.giveCouponTemplateCounts()))
                .toList());
    }
}
