package com.lemnt.mall.domain.promotion.model;

/**
 * Domain failure; an HTTP adapter must map it explicitly rather than exposing exception text.
 */
public final class PricingException extends IllegalArgumentException {
    /**
     * 编码。
     */
    public enum Code {
        INVALID_PROMOTION_REQUEST,
        AMOUNT_OUT_OF_RANGE,
        COUPON_NOT_ELIGIBLE,
        PROMOTION_CANDIDATE_LIMIT
    }

    private final Code code;

    /**
     * 创建计价异常实例。
     */
    public PricingException(Code code) {
        super(code.name());
        this.code = code;
    }

    /**
     * 编码。
     */
    public Code code() {
        return code;
    }

    /**
     * 校验并获取当前会员身份。
     */
    public static void require(boolean condition) {
        if (!condition) {
            throw new PricingException(Code.INVALID_PROMOTION_REQUEST);
        }
    }
}
