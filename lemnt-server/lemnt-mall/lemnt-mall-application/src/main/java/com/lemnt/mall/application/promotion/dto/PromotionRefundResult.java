package com.lemnt.mall.application.promotion.dto;

/** 活动订单退款结果：本次退回积分及累计已退数量。 */
public record PromotionRefundResult(long refundPoints, int refundedQuantity) {
    public static PromotionRefundResult none() {
        return new PromotionRefundResult(0L, 0);
    }
}
