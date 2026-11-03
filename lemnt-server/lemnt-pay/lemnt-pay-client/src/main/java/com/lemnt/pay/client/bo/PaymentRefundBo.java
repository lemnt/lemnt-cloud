package com.lemnt.pay.client.bo;

/** 原售后请求标识与固定金额，必须再核对订单服务冻结计划。 */
public record PaymentRefundBo(
        String afterSaleNo,
        String orderNo,
        String paymentNo,
        Long memberId,
        java.math.BigDecimal amount,
        Integer ruleVersion)
        implements java.io.Serializable {}
