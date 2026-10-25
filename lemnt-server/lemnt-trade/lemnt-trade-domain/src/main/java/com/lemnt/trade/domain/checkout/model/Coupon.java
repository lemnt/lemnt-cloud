package com.lemnt.trade.domain.checkout.model;

public record Coupon(Long id, Long templateId, Long memberId, String status, String orderNo) {}
