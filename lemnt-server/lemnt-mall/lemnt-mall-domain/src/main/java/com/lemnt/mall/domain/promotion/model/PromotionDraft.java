package com.lemnt.mall.domain.promotion.model;

/**
 * 营销草稿。
 */
public record PromotionDraft(
        String name,
        PromotionScope scope,
        DiscountRule rule,
        PromotionVersion.CouponPolicy couponPolicy,
        int priority,
        long startsAtEpochMs,
        long endsAtEpochMs,
        Long globalUseLimit,
        Long memberUseLimit
) {
    /**
     * 创建营销草稿实例。
     */
    public PromotionDraft {
        PricingException.require(
                name != null
                && !name.isBlank()
                && name.length() <= 100
                && name.equals(name.trim()));
        PricingException.require(priority >= 0 && priority <= 1000);
        new PromotionVersion(
                1, 1, priority, couponPolicy, scope, rule, startsAtEpochMs, endsAtEpochMs);
        PricingException.require(globalUseLimit == null || globalUseLimit > 0);
        PricingException.require(memberUseLimit == null || memberUseLimit > 0);
        PricingException.require(
                globalUseLimit == null
                || memberUseLimit == null
                || memberUseLimit <= globalUseLimit);
    }

    /**
     * 版本。
     */
    public PromotionVersion version(long id, int number) {
        return new PromotionVersion(
                id, number, priority, couponPolicy, scope, rule, startsAtEpochMs, endsAtEpochMs);
    }
}
