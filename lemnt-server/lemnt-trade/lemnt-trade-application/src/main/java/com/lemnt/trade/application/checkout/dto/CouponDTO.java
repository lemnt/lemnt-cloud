package com.lemnt.trade.application.checkout.dto;

public record CouponDTO(Long id, Long templateId, Long memberId, String status, String orderNo) {}
