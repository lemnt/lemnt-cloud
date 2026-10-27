package com.lemnt.mall.domain.promotion.model;

/**
 * 营销根节点。
 */
public record PromotionRoot(
        long id,
        String code,
        State state,
        long revision,
        Integer activeVersion,
        int lastVersion,
        PromotionDraft draft
) {
    /**
     * 状态。
     */
    public enum State {
        DRAFT,
                PUBLISHED,
                PAUSED,
                ARCHIVED
    }

    /**
     * 回执。
     */
    public PromotionReceipt receipt() {
        return new PromotionReceipt(id, revision, state, activeVersion, lastVersion);
    }
}
