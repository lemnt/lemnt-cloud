package com.lemnt.order.client.dto;

/** 订单持久化的冻结退款请求，现金为零时不产生支付退款流水。 */
public record FrozenRefundDTO(
        String afterSaleNo,
        String orderNo,
        Long memberId,
        String paymentNo,
        java.math.BigDecimal amount,
        Integer ruleVersion,
        String status)
        implements java.io.Serializable {}
