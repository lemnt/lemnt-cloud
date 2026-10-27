package com.lemnt.mall.domain.promotion.model;

/**
 * 营销使用。
 */
public record PromotionUsage(
        String orderNo,
        long rootId,
        int version,
        long memberId,
        long discountFen,
        String ruleHash,
        State state
) {
    /**
     * 状态。
     */
    public enum State {
        HELD,
        USED,
        RELEASED
    }
}
