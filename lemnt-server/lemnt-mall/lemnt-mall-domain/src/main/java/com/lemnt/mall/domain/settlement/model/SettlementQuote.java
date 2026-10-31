package com.lemnt.mall.domain.settlement.model;

import java.math.BigDecimal;
import java.util.List;

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
        SettlementDelivery delivery
) {
    /**
     * 创建结算报价实例。
     */
    public SettlementQuote {
        items = List.copyOf(items);
    }

    /** 存量快照没有积分权益，按零积分订单读取。 */
    public SettlementQuote(List<SettlementItem> items, BigDecimal originalAmount, BigDecimal discountAmount,
        BigDecimal payableAmount, Long couponId
    ) {
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
        this(items, originalAmount, discountAmount, payableAmount, couponId, usePoint, givePoint, null);
    }
}
