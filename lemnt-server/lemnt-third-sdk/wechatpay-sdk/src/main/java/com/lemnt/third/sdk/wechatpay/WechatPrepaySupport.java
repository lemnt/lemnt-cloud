package com.lemnt.third.sdk.wechatpay;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 微信下单、退款共用的金额与文本规则，微信侧字段长度和精度限制在此收敛。
 */
final class WechatPrepaySupport {
    static final String CURRENCY = "CNY";
    private static final int MAX_DESCRIPTION_LENGTH = 127;
    private static final int MAX_FEN_DIGITS = 2;

    private WechatPrepaySupport() {
    }

    static void requireText(String value, int maxLength, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + "不能为空");
        }
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(name + "长度不能超过" + maxLength);
        }
    }

    /**
     * 金额必须为正且最多两位小数，避免渠道按分截断。
     */
    static void requireAmount(BigDecimal amount, String name) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(name + "必须大于0");
        }
        if (amount.scale() > MAX_FEN_DIGITS && amount.stripTrailingZeros().scale() > MAX_FEN_DIGITS) {
            throw new IllegalArgumentException(name + "最多两位小数");
        }
    }

    /**
     * 元转分，供支付下单使用。
     */
    static int fenInt(BigDecimal amount, String name) {
        requireAmount(amount, name);
        try {
            return amount.setScale(MAX_FEN_DIGITS, RoundingMode.UNNECESSARY)
                .movePointRight(MAX_FEN_DIGITS)
                .intValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(name + "超出微信支付允许范围");
        }
    }

    /**
     * 元转分，供退款使用。
     */
    static long fenLong(BigDecimal amount, String name) {
        requireAmount(amount, name);
        try {
            return amount.setScale(MAX_FEN_DIGITS, RoundingMode.UNNECESSARY)
                .movePointRight(MAX_FEN_DIGITS)
                .longValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(name + "超出微信支付允许范围");
        }
    }

    /**
     * 分转元，渠道金额统一两位小数。
     */
    static BigDecimal yuan(long fen) {
        return BigDecimal.valueOf(fen).movePointLeft(MAX_FEN_DIGITS).setScale(MAX_FEN_DIGITS);
    }

    /**
     * 账单展示说明，微信限制 127 字符。
     */
    static String description(String orderNo) {
        String value = "订单" + orderNo;
        return value.length() <= MAX_DESCRIPTION_LENGTH
            ? value
            : value.substring(0, MAX_DESCRIPTION_LENGTH);
    }
}
