package com.lemnt.mall.domain.promotion.model;

/**
 * Immutable published candidate. Publication state, audience and quota reads belong upstream.
 */
public record PromotionVersion(
        long id,
        int version,
        int priority,
        CouponPolicy couponPolicy,
        PromotionScope scope,
        DiscountRule rule,
        long startsAtEpochMs,
        long endsAtEpochMs
) {
    /**
     * 优惠券策略。
     */
    public enum CouponPolicy {
        EXCLUSIVE,
        ALLOW_ONE
    }

    /**
     * 创建营销版本实例。
     */
    public PromotionVersion {
        PricingException.require(id > 0 && version > 0 && couponPolicy != null && scope != null);
        PricingException.require(
                rule instanceof DiscountRule.FullReduction
                || rule instanceof DiscountRule.Percentage);
        PricingException.require(startsAtEpochMs > 0 && endsAtEpochMs > startsAtEpochMs);
    }

    /**
     * 有效时点。
     */
    public boolean activeAt(long nowEpochMs) {
        return startsAtEpochMs <= nowEpochMs && nowEpochMs < endsAtEpochMs;
    }
}
