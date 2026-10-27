package com.lemnt.mall.domain.promotion.model;

/**
 * 已发布营销。
 */
public record PublishedPromotion(
        long rootId,
        int version,
        PromotionDraft draft,
        String ruleHash,
        String algorithmVersion,
        long operatorId,
        long publishedAtEpochMs
) {}
