package com.lemnt.trade.application.checkout.cmd;

public record CheckoutSubmitCmd(
        Long memberId,
        String requestNo,
        java.util.List<Long> productIds,
        Long couponId,
        String quoteToken) {}
