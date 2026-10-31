package com.lemnt.trade.application.payment.qry;

/**
 * 会员支付状态查询。
 */
public record MemberPaymentStatusQry(
        Long memberId,
        String orderNo
) implements java.io.Serializable {}
