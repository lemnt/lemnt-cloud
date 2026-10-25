package com.lemnt.trade.domain.promotion.model;

/** Immutable published candidate. Publication state, audience and quota reads belong upstream. */
public record PromotionVersion(
        long id,
        int version,
        int priority,
        CouponPolicy couponPolicy,
        PromotionScope scope,
        DiscountRule rule,
        long startsAtEpochMs,
        long endsAtEpochMs) {
    public enum CouponPolicy {
        EXCLUSIVE,
        ALLOW_ONE
    }

    public PromotionVersion {
        PricingException.require(id > 0 && version > 0 && couponPolicy != null && scope != null);
        PricingException.require(
                rule instanceof DiscountRule.FullReduction
                        || rule instanceof DiscountRule.Percentage);
        PricingException.require(startsAtEpochMs > 0 && endsAtEpochMs > startsAtEpochMs);
    }

    public boolean activeAt(long nowEpochMs) {
        return startsAtEpochMs <= nowEpochMs && nowEpochMs < endsAtEpochMs;
    }
}
