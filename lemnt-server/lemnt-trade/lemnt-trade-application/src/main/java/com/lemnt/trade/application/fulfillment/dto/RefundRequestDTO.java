package com.lemnt.trade.application.fulfillment.dto;

public record RefundRequestDTO(
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
