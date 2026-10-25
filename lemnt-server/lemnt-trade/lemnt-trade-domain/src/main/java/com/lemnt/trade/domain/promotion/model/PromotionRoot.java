package com.lemnt.trade.domain.promotion.model;

public record PromotionRoot(
        long id,
        String code,
        State state,
        long revision,
        Integer activeVersion,
        int lastVersion,
        PromotionDraft draft) {
    public enum State {
        DRAFT,
        PUBLISHED,
        PAUSED,
        ARCHIVED
    }

    public PromotionAdminReceipt receipt() {
        return new PromotionAdminReceipt(id, revision, state, activeVersion, lastVersion);
    }
}
