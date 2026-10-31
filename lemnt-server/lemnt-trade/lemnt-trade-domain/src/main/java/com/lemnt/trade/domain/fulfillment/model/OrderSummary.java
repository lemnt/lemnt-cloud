package com.lemnt.trade.domain.fulfillment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单汇总。
 */
public record OrderSummary(
        String orderNo,
        Long memberId,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createTime,
        boolean inventoryConfirmed,
        boolean activeRefund,
        String carrierCode,
        String trackingNo,
        LocalDateTime shippedAt,
        LocalDateTime receivedAt
) {}
