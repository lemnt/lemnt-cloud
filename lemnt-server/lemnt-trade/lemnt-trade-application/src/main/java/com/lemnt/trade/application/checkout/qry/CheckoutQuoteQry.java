package com.lemnt.trade.application.checkout.qry;

public record CheckoutQuoteQry(Long memberId, java.util.List<Long> productIds, Long couponId) {}
