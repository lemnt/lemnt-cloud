package com.lemnt.order.domain.settlement.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 结算报价。
 */
public record SettlementQuote(
        List<SettlementItem> items,
        BigDecimal originalAmount,
        BigDecimal discountAmount,
        BigDecimal payableAmount,
        Long couponId,
        long usePoint,
        long givePoint,
        SettlementDelivery delivery,
        long rewardPoint,
        boolean freeDelivery,
        Map<Long, Integer> giveCouponTemplateCounts,
        List<SettlementPromotion> promotions,
        BigDecimal deliveryOriginalPrice,
        BigDecimal deliveryDiscountPrice) {
    /** 兼容未记录本批优惠与退款元数据的历史快照。 */
    public SettlementQuote(
            List<SettlementItem> items,
            BigDecimal originalAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            Long couponId,
            long usePoint,
            long givePoint,
            SettlementDelivery delivery,
            long rewardPoint,
            boolean freeDelivery,
            Map<Long, Integer> giveCouponTemplateCounts) {
        this(
                items,
                originalAmount,
                discountAmount,
                payableAmount,
                couponId,
                usePoint,
                givePoint,
                delivery,
                rewardPoint,
                freeDelivery,
                giveCouponTemplateCounts,
                List.of(),
                null,
                null);
    }

    /**
     * 创建结算报价实例。
     */
    public SettlementQuote {
        promotions = promotions == null ? List.of() : List.copyOf(promotions);
        items = List.copyOf(items);
        giveCouponTemplateCounts =
                giveCouponTemplateCounts == null ? Map.of() : Map.copyOf(giveCouponTemplateCounts);
    }

    /** 存量快照没有营销赠礼，按无赠礼报价读取。 */
    public SettlementQuote(
            List<SettlementItem> items,
            BigDecimal originalAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            Long couponId,
            long usePoint,
            long givePoint,
            SettlementDelivery delivery) {
        this(
                items,
                originalAmount,
                discountAmount,
                payableAmount,
                couponId,
                usePoint,
                givePoint,
                delivery,
                0L,
                false,
                Map.of());
    }

    /** 存量快照没有积分权益，按零积分订单读取。 */
    public SettlementQuote(
            List<SettlementItem> items,
            BigDecimal originalAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            Long couponId) {
        this(items, originalAmount, discountAmount, payableAmount, couponId, 0L, 0L);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public SettlementQuote(
            List<SettlementItem> items,
            BigDecimal originalAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            Long couponId,
            long usePoint,
            long givePoint) {
        this(
                items,
                originalAmount,
                discountAmount,
                payableAmount,
                couponId,
                usePoint,
                givePoint,
                null);
    }
}
