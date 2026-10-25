package com.lemnt.trade.application.fulfillment.dto;

public record OrderSummaryDTO(
        String orderNo,
        Long memberId,
        java.math.BigDecimal totalAmount,
        String status,
        java.time.LocalDateTime createTime,
        boolean inventoryConfirmed,
        boolean activeRefund,
        String carrierCode,
        String trackingNo,
        java.time.LocalDateTime shippedAt,
        java.time.LocalDateTime receivedAt) {}
