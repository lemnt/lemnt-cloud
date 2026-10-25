package com.lemnt.trade.domain.promotion.model;

public final class PromotionPersistenceException extends IllegalStateException {
    public enum Code {
        PROMOTION_NOT_FOUND,
        PROMOTION_REVISION_CONFLICT,
        ADMIN_REQUEST_CONFLICT,
        PROMOTION_SNAPSHOT_STALE,
        PROMOTION_QUOTA_CHANGED,
        PROMOTION_STATE_CONFLICT,
        PROMOTION_LIMIT_BELOW_USAGE,
        PROMOTION_USAGE_CONFLICT
    }

    private final Code code;

    public PromotionPersistenceException(Code code) {
        super(code.name());
        this.code = code;
    }

    public Code code() {
        return code;
    }
}
