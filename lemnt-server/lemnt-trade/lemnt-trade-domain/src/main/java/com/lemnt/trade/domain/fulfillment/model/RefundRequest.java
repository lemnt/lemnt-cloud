package com.lemnt.trade.domain.fulfillment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款请求。
 */
public record RefundRequest(
        String refundNo,
        String orderNo,
        String requestNo,
        Long memberId,
        String paymentNo,
        BigDecimal amount,
        String reason,
        String status,
        String decisionReason,
        LocalDateTime requestedAt,
        LocalDateTime decidedAt
) {}
