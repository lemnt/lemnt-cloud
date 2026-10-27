package com.lemnt.mall.domain.promotion.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

/**
 * All eligibility facts and the single clock sample are server supplied, never HTTP input.
 */
public record PricingContext(
        List<PricingLine> lines,
        List<PromotionVersion> campaigns,
        SelectedCoupon coupon,
        Mode promotionMode,
        long nowEpochMs
) {
    /**
     * 模式。
     */
    public enum Mode {
        BEST,
        NONE
    }

    /**
     * 创建计价上下文实例。
     */
    public PricingContext {
        PricingException.require(lines != null && !lines.isEmpty() && lines.size() <= 100);
        PricingException.require(campaigns != null && promotionMode != null && nowEpochMs > 0);
        var skuIds = new HashSet<Long>();
        for (var line : lines) {
            PricingException.require(line != null && skuIds.add(line.productId()));
        }
        var versions = new HashSet<String>();
        for (var campaign : campaigns) {
            PricingException.require(
                    campaign != null && versions.add(campaign.id() + ":" + campaign.version()));
        }
        lines =
        lines.stream()
            .sorted(Comparator.comparingLong(PricingLine::productId))
            .toList();
        campaigns = List.copyOf(campaigns);
    }
}
