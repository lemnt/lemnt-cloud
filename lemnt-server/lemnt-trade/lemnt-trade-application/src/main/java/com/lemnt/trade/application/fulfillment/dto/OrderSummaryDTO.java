package com.lemnt.trade.application.fulfillment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单汇总数据传输对象。
 */
public record OrderSummaryDTO(
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
) implements java.io.Serializable {}
