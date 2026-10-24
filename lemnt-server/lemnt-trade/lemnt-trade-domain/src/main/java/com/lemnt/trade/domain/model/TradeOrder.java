package com.lemnt.trade.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 交易订单聚合根：订单金额和商品信息来自服务端快照。 */
public record TradeOrder(Long id, String orderNo, Long memberId, BigDecimal totalAmount,
                         String status, List<TradeOrderItem> items, LocalDateTime createTime) {
    public TradeOrder { items = items == null ? List.of() : List.copyOf(items); }
    public static TradeOrder pendingPayment(String orderNo, Long memberId, BigDecimal totalAmount,
                                            List<TradeOrderItem> items) {
        if (orderNo == null || orderNo.isBlank()) throw new IllegalArgumentException("订单号不能为空");
        if (memberId == null || memberId <= 0) throw new IllegalArgumentException("会员ID不合法");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("订单明细不能为空");
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("订单金额必须大于0");
        return new TradeOrder(null, orderNo, memberId, totalAmount, "PENDING_PAYMENT", items, null);
    }
}