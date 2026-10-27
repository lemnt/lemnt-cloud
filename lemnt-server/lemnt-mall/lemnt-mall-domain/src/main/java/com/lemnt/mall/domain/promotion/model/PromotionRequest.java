package com.lemnt.mall.domain.promotion.model;

/**
 * 营销后台请求。
 */
public record PromotionRequest(
        String action,
        String fingerprint,
        PromotionReceipt receipt
) {}
