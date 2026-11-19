package com.lemnt.order.domain.order.model;

/** 用订单主键为游标扫描已准备但未确认支付的订单。 */
public record PaymentRecoveryCandidate(long id, String orderNo) {}
