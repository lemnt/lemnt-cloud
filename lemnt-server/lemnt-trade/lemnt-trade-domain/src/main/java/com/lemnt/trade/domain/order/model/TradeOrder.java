package com.lemnt.trade.domain.order.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易订单聚合根：订单金额和商品信息来自服务端快照。
 */
public record TradeOrder(
        Long id,
        String orderNo,
        Long memberId,
        BigDecimal totalAmount,
        String status,
        List<TradeOrderItem> items,
        LocalDateTime createTime,
        long usePoint,
        long givePoint,
        TradeOrderDelivery delivery
) {
    /**
     * 创建交易订单实例。
     */
    public TradeOrder {
        items = items == null ? List.of() : List.copyOf(items);
    }

    /**
     * 待处理支付。
     */
    public static TradeOrder pendingPayment(String orderNo, Long memberId, BigDecimal totalAmount,
            List<TradeOrderItem> items
    ) {
        if (orderNo == null || orderNo.isBlank()
        ) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        if (memberId == null || memberId <= 0
        ) {
            throw new IllegalArgumentException("会员ID不合法");
        }
        if (items == null || items.isEmpty()
        ) {
            throw new IllegalArgumentException("订单明细不能为空");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0
        ) {
            throw new IllegalArgumentException("订单金额必须大于0");
        }
        return new TradeOrder(null, orderNo, memberId, totalAmount, "PENDING_PAYMENT", items, null);
    }

    /** 存量快照没有积分权益，按零积分订单读取。 */
    public TradeOrder(
            Long id,
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            String status,
            List<TradeOrderItem> items,
            LocalDateTime createTime) {
        this(id, orderNo, memberId, totalAmount, status, items, createTime, 0L, 0L);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public TradeOrder(
            Long id,
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            String status,
            List<TradeOrderItem> items,
            LocalDateTime createTime,
            long usePoint,
            long givePoint) {
        this(id, orderNo, memberId, totalAmount, status, items, createTime, usePoint, givePoint, null);
    }
}
