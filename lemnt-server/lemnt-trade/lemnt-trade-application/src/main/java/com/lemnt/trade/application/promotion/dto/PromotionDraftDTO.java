package com.lemnt.trade.application.promotion.dto;

public record PromotionDraftDTO(
        String name,
        String type,
        String scopeKind,
        java.util.List<Long> scopeIds,
        String couponPolicy,
        int priority,
        long startsAtEpochMs,
        long endsAtEpochMs,
        Long globalUseLimit,
        Long memberUseLimit,
        java.util.List<PromotionTierDTO> tiers,
        Long thresholdFen,
        Integer payRateBps,
        Long maxDiscountFen) {}
