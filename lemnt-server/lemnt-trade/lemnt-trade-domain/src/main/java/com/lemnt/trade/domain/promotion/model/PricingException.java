package com.lemnt.trade.domain.promotion.model;

/** Domain failure; an HTTP adapter must map it explicitly rather than exposing exception text. */
public final class PricingException extends IllegalArgumentException {
    public enum Code {
        INVALID_PROMOTION_REQUEST,
        AMOUNT_OUT_OF_RANGE,
        COUPON_NOT_ELIGIBLE,
        PROMOTION_CANDIDATE_LIMIT
    }

    private final Code code;

    public PricingException(Code code) {
        super(code.name());
        this.code = code;
    }

    public Code code() {
        return code;
    }

    public static void require(boolean condition) {
        if (!condition) throw new PricingException(Code.INVALID_PROMOTION_REQUEST);
    }
}
