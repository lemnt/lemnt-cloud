package com.lemnt.trade.domain.promotion.model;

public record PromotionUsage(
        String orderNo,
        long rootId,
        int version,
        long memberId,
        long discountFen,
        String ruleHash,
        State state) {
    public enum State {
        HELD,
        USED,
        RELEASED
    }
}
