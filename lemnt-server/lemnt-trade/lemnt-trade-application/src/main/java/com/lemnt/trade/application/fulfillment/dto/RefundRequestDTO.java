package com.lemnt.trade.application.fulfillment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款请求数据传输对象。
 */
public record RefundRequestDTO(
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
) implements java.io.Serializable {}
