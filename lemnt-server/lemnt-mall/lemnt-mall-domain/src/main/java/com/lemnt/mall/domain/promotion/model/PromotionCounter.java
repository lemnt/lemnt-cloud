package com.lemnt.mall.domain.promotion.model;

/**
 * 营销计数器。
 */
public record PromotionCounter(
        long held,
        long used,
        Long globalLimit,
        Long memberLimit
) {}
