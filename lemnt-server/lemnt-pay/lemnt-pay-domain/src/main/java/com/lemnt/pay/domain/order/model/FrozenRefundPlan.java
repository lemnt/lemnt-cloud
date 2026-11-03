package com.lemnt.pay.domain.order.model;

/** 原售后冻结请求，支付服务不得按当前配置重算。 */
public record FrozenRefundPlan(
        String afterSaleNo,
        String orderNo,
        Long memberId,
        String paymentNo,
        java.math.BigDecimal amount,
        Integer ruleVersion,
        String status)
        implements java.io.Serializable {}
