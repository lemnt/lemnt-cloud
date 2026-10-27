package com.lemnt.mall.domain.promotion.model;

import java.util.List;

/**
 * 计价结果。
 */
public record PricingResult(
        String algorithmVersion,
        long originalFen,
        long campaignDiscountFen,
        long couponDiscountFen,
        long payableFen,
        PromotionVersion selectedCampaign,
        Long couponId,
        List<Line> lines
) {
    /**
     * 创建计价结果实例。
     */
    public PricingResult {
        lines = List.copyOf(lines);
    }

    /**
     * 判断计价结果是否可提交。
     */
    public boolean submittable() {
        return payableFen > 0;
    }

    /**
     * 不可提交的原因。
     */
    public String blockedReason() {
        return submittable() ? null : "ZERO_PAY_UNSUPPORTED";
    }

    /**
     * 明细。
     */
    public record Line(
            long productId,
            long spuId,
            long unitPriceFen,
            int quantity,
            long grossFen,
            long campaignDiscountFen,
            long couponDiscountFen,
            long payableFen,
            long unitBasePayableFen,
            int extraFenUnitCount
    ) {
        /**
         * Stable unit ordinal for future refund limits; this does not execute a refund.
         */
        public long unitPayableFen(int ordinal) {
            PricingException.require(ordinal >= 1 && ordinal <= quantity);
            return unitBasePayableFen +(ordinal <= extraFenUnitCount ? 1 : 0);
        }
    }
}
