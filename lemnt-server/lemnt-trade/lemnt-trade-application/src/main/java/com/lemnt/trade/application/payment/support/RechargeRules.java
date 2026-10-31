package com.lemnt.trade.application.payment.support;

import com.lemnt.trade.domain.payment.model.PaymentChannel;
import com.lemnt.trade.domain.payment.model.WalletRechargeStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

/** 充值套餐与充值单入参校验。 */
public final class RechargeRules {
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999.99");

    private RechargeRules() {
    }

    public static String name(String value) {
        return text(value, 64, "套餐名");
    }

    public static String text(String value, int max, String field) {
        if (value == null || value.isBlank() || value.trim().length() > max) {
            throw new IllegalArgumentException(field + "不能为空且不超过" + max + "字");
        }
        return value.trim();
    }

    public static void status(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0启用或1停用");
        }
    }

    /** 支付金额必须大于0，最多两位小数。 */
    public static BigDecimal positiveMoney(BigDecimal value, String field) {
        BigDecimal result = money(value, field);
        if (result.signum() <= 0) {
            throw new IllegalArgumentException(field + "必须大于0");
        }
        return result;
    }

    /** 赠送金额可以为0，不能为负。 */
    public static BigDecimal nonNegativeMoney(BigDecimal value, String field) {
        BigDecimal result = money(value, field);
        if (result.signum() < 0) {
            throw new IllegalArgumentException(field + "不能为负");
        }
        return result;
    }

    public static String statusText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            WalletRechargeStatus.valueOf(value);
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("充值状态不合法");
        }
        return value;
    }

    /** 可选渠道编码，只接受系统声明支持的渠道。 */
    public static String channel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.trim();
        if (Arrays.stream(PaymentChannel.values()).noneMatch(channel -> channel.name().equals(text))) {
            throw new IllegalArgumentException("不支持的渠道编码");
        }
        return text;
    }

    private static BigDecimal money(BigDecimal value, String field) {
        if (value == null || value.abs().compareTo(MAX_AMOUNT) > 0
                || value.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(field + "必须为0至999999999.99之间的两位小数");
        }
        try {
            return value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException error) {
            throw new IllegalArgumentException(field + "必须为两位小数");
        }
    }
}
