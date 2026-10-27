package com.lemnt.mall.domain.promotion.model;

/**
 * 营销持久化异常。
 */
public final class PromotionPersistenceException extends IllegalStateException {
    /**
     * 编码。
     */
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

    /**
     * 创建营销持久化异常实例。
     */
    public PromotionPersistenceException(Code code) {
        super(code.name());
        this.code = code;
    }

    /**
     * 编码。
     */
    public Code code() {
        return code;
    }
}
