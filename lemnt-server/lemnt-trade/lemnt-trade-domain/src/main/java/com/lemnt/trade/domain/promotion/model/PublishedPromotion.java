package com.lemnt.trade.domain.promotion.model;

public record PublishedPromotion(
        long rootId,
        int version,
        PromotionDraft draft,
        String ruleHash,
        String algorithmVersion,
        long operatorId,
        long publishedAtEpochMs) {}
