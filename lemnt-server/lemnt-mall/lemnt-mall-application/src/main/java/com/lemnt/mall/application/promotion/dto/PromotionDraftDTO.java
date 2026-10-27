package com.lemnt.mall.application.promotion.dto;

import java.util.List;

/**
 * 营销草稿数据传输对象。
 */
public record PromotionDraftDTO(
        String name,
        String type,
        String scopeKind,
        List<Long> scopeIds,
        String couponPolicy,
        int priority,
        long startsAtEpochMs,
        long endsAtEpochMs,
        Long globalUseLimit,
        Long memberUseLimit,
        List<PromotionTierDTO> tiers,
        Long thresholdFen,
        Integer payRateBps,
        Long maxDiscountFen
) implements java.io.Serializable {}
