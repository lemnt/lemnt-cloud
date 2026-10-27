package com.lemnt.trade.application.payment.dto;

import java.math.BigDecimal;

/**
 * 会员支付状态数据传输对象。
 */
public record MemberPaymentStatusDTO(
        String orderNo,
        String orderStatus,
        BigDecimal payableAmount,
        String localPaymentStatus,
        boolean channelVerified
) implements java.io.Serializable {}
