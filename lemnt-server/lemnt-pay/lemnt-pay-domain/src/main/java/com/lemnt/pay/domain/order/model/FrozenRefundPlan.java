package com.lemnt.pay.domain.order.model;

import java.io.Serializable;
import java.math.BigDecimal;

/** 原售后冻结请求，支付服务不得按当前配置重算。 */
public record FrozenRefundPlan(
        String afterSaleNo,
        String orderNo,
        Long memberId,
        String paymentNo,
        BigDecimal amount,
        Integer ruleVersion,
        String status)
        implements Serializable {}
