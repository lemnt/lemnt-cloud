package com.lemnt.pay.domain.order.model;

/** 支付侧防腐层，只保留校验资金归属所需的原订单事实。 */
public record OrderPaymentSnapshot(
        String orderNo, Long memberId, java.math.BigDecimal totalAmount, String status)
        implements java.io.Serializable {}
