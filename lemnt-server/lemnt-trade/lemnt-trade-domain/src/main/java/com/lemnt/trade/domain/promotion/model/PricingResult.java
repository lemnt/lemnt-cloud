package com.lemnt.trade.domain.promotion.model;

import java.util.List;

public record PricingResult(
        String algorithmVersion,
        long originalFen,
        long campaignDiscountFen,
        long couponDiscountFen,
        long payableFen,
        PromotionVersion selectedCampaign,
        Long couponId,
        List<Line> lines) {
    public PricingResult {
        lines = List.copyOf(lines);
    }

    public boolean submittable() {
        return payableFen > 0;
    }

    public String blockedReason() {
        return submittable() ? null : "ZERO_PAY_UNSUPPORTED";
    }

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
            int extraFenUnitCount) {
        /** Stable unit ordinal for future refund limits; this does not execute a refund. */
        public long unitPayableFen(int ordinal) {
            PricingException.require(ordinal >= 1 && ordinal <= quantity);
            return unitBasePayableFen + (ordinal <= extraFenUnitCount ? 1 : 0);
        }
    }
}
