package com.lemnt.mall.domain.promotion.model;

import java.math.BigDecimal;

/**
 * Exact legacy yuan boundary and checked nonnegative fen arithmetic.
 */
public final class FenAmounts {
    private FenAmounts() {}

    /**
     * 从元金额。
     */
    public static long fromYuan(BigDecimal yuan) {
        if (yuan == null || yuan.signum() < 0) {
            throw outOfRange();
        }
        try {
            return yuan.movePointRight(2).longValueExact();
        } catch (ArithmeticException ex) {
            throw outOfRange();
        }
    }

    /**
     * 新增。
     */
    public static long add(long left, long right) {
        if (left < 0 || right < 0) {
            throw outOfRange();
        }
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException ex) {
            throw outOfRange();
        }
    }

    /**
     * 计算金额乘积。
     */
    public static long multiply(long amount, int quantity) {
        if (amount < 0 || quantity < 0) {
            throw outOfRange();
        }
        try {
            return Math.multiplyExact(amount, quantity);
        } catch (ArithmeticException ex) {
            throw outOfRange();
        }
    }

    private static PricingException outOfRange() {
        return new PricingException(PricingException.Code.AMOUNT_OUT_OF_RANGE);
    }
}
