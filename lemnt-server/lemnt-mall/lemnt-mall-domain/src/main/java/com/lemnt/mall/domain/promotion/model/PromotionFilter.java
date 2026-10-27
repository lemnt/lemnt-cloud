package com.lemnt.mall.domain.promotion.model;

import java.util.Set;

/**
 * 营销筛选。
 */
public record PromotionFilter(
        int pageNum,
        int pageSize,
        String state,
        String code
) {
    /**
     * 创建营销筛选实例。
     */
    public PromotionFilter {
        PricingException.require(
                pageNum >= 1 && pageNum <= 1000000 && pageSize >= 1 && pageSize <= 100);
        PricingException.require(
                state == null
                || Set.of("DRAFT", "PUBLISHED", "PAUSED", "ARCHIVED")
                .contains(state));
        PricingException.require(code == null || code.matches("[A-Z0-9][A-Z0-9_-]{0,63}"));
    }

    /**
     * 分页条数。
     */
    public int limit() {
        return pageSize;
    }

    /**
     * 分页偏移量。
     */
    public long offset() {
        return (pageNum - 1L) * pageSize;
    }
}
