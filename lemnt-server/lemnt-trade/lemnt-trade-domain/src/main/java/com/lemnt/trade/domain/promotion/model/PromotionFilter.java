package com.lemnt.trade.domain.promotion.model;

public record PromotionFilter(int pageNum, int pageSize, String state, String code) {
    public PromotionFilter {
        PricingException.require(
                pageNum >= 1 && pageNum <= 1000000 && pageSize >= 1 && pageSize <= 100);
        PricingException.require(
                state == null
                        || java.util.Set.of("DRAFT", "PUBLISHED", "PAUSED", "ARCHIVED")
                                .contains(state));
        PricingException.require(code == null || code.matches("[A-Z0-9][A-Z0-9_-]{0,63}"));
    }

    public int limit() {
        return pageSize;
    }

    public long offset() {
        return (pageNum - 1L) * pageSize;
    }
}
