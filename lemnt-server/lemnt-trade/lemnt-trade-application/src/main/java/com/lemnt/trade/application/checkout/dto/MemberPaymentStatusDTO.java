package com.lemnt.trade.application.checkout.dto;

public record MemberPaymentStatusDTO(
        String orderNo,
        String orderStatus,
        java.math.BigDecimal payableAmount,
        String localPaymentStatus,
        boolean channelVerified) {}
