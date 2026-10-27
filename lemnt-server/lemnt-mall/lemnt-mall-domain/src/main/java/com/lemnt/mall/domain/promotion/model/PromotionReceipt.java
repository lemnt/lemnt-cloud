package com.lemnt.mall.domain.promotion.model;

/**
 * 营销后台回执。
 */
public record PromotionReceipt(
        long id,
        long revision,
        PromotionRoot.State state,
        Integer activeVersion,
        int lastVersion
) {}
