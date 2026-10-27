package com.lemnt.mall.domain.promotion.model;

/**
 * Trusted catalog facts, not a client request. Availability is checked by the application.
 */
public record PricingLine(
        long productId,
        long spuId,
        long unitPriceFen,
        int quantity
) {
    /**
     * 创建计价明细实例。
     */
    public PricingLine {
        PricingException.require(productId > 0 && spuId > 0 && quantity >= 1 && quantity <= 999);
        if (unitPriceFen <= 0) {
            throw new PricingException(PricingException.Code.AMOUNT_OUT_OF_RANGE);
        }
    }

    /**
     * 计算原价金额（分）。
     */
    public long grossFen() {
        return FenAmounts.multiply(unitPriceFen, quantity);
    }
}
