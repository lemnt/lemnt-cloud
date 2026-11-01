package com.lemnt.mall.application.settlement.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 结算报价数据传输对象。
 */
public record SettlementQuoteDTO(
        List<SettlementItemDTO> items,
        BigDecimal originalAmount,
        BigDecimal discountAmount,
        BigDecimal payableAmount,
        Long couponId,
        String quoteToken,
        long usePoint,
        long givePoint,
        SettlementDeliveryDTO delivery,
        boolean freeDelivery,
        Map<Long, Integer> giveCouponTemplateCounts
) implements Serializable {
    /** 兼容无营销赠礼的报价快照。 */
    public SettlementQuoteDTO(
            List<SettlementItemDTO> items,
            BigDecimal originalAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            Long couponId,
            String quoteToken,
            long usePoint,
            long givePoint,
            SettlementDeliveryDTO delivery) {
        this(items, originalAmount, discountAmount, payableAmount, couponId, quoteToken, usePoint, givePoint,
                delivery, false, Map.of());
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public SettlementQuoteDTO(
            List<SettlementItemDTO> items,
            BigDecimal originalAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            Long couponId,
            String quoteToken,
            long usePoint,
            long givePoint) {
        this(items, originalAmount, discountAmount, payableAmount, couponId, quoteToken, usePoint, givePoint, null);
    }
}
