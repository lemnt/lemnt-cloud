package com.lemnt.pay.domain.payment.model;

import java.math.BigDecimal;

/** 业务支付主单快照，历史金额或归属无法核实时可为空。 */
public record PaymentOrder(
        String paymentOrderNo,
        String orderNo,
        Long memberId,
        BigDecimal amount,
        String status,
        String paidPaymentNo) {}
