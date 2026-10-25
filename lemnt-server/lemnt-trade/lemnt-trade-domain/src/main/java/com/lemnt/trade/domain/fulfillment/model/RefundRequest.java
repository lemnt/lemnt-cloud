package com.lemnt.trade.domain.fulfillment.model;

public record RefundRequest(
        String refundNo,
        String orderNo,
        String requestNo,
        Long memberId,
        String paymentNo,
        java.math.BigDecimal amount,
        String reason,
        String status,
        String decisionReason,
        java.time.LocalDateTime requestedAt,
        java.time.LocalDateTime decidedAt) {}
