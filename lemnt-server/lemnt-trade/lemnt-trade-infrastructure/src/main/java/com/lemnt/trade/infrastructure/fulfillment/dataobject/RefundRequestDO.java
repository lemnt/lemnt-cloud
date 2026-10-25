package com.lemnt.trade.infrastructure.fulfillment.dataobject;

import lombok.Data;

@Data
public class RefundRequestDO {
    private String refundNo;
    private String orderNo;
    private String requestNo;
    private Long memberId;
    private String paymentNo;
    private java.math.BigDecimal amount;
    private String reason;
    private String status;
    private String decisionReason;
    private java.time.LocalDateTime requestedAt;
    private java.time.LocalDateTime decidedAt;
}
