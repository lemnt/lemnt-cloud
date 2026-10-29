package com.lemnt.mall.application.settlement.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 结算回执数据传输对象。
 */
public record SettlementReceiptDTO(
        String orderNo,
        Long memberId,
        String status,
        BigDecimal originalAmount,
        BigDecimal discountAmount,
        BigDecimal payableAmount,
        Long couponId,
        List<SettlementItemDTO> items,
        SettlementDeliveryDTO delivery,
        Boolean commentStatus,
        java.time.LocalDateTime finishTime
) implements Serializable {
    /** 兼容报价/历史快照，订单项和评价元数据由持久化后读取。 */
    public SettlementReceiptDTO(
            String orderNo,
            Long memberId,
            String status,
            BigDecimal originalAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            Long couponId,
            List<SettlementItemDTO> items,
            SettlementDeliveryDTO delivery
    ) {
        this(orderNo, memberId, status, originalAmount, discountAmount, payableAmount, couponId, items, delivery, null, null);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public SettlementReceiptDTO(
            String orderNo,
            Long memberId,
            String status,
            BigDecimal originalAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            Long couponId,
            List<SettlementItemDTO> items) {
        this(orderNo, memberId, status, originalAmount, discountAmount, payableAmount, couponId, items, null);
    }
}
