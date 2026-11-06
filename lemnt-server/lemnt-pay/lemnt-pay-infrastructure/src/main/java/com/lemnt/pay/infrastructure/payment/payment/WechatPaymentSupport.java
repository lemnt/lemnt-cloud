package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.common.core.exception.ServiceException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 微信支付下单共用的金额换算与商品说明规则。
 */
final class WechatPaymentSupport {
    static final String CURRENCY = "CNY";
    private static final int MAX_DESCRIPTION_LENGTH = 127;

    private WechatPaymentSupport() {
    }

    /**
     * 金额换算为分；非两位小数或超出范围直接失败，不四舍五入。
     */
    static int cents(BigDecimal amount) {
        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).intValueExact();
        } catch (ArithmeticException exception) {
            throw new ServiceException("支付金额精度不合法", 400);
        }
    }

    /**
     * 账单展示用说明，微信限制 127 字符，超长按上限截断。
     */
    static String description(String orderNo) {
        String value = "订单" + orderNo;
        return value.length() <= MAX_DESCRIPTION_LENGTH
            ? value
            : value.substring(0, MAX_DESCRIPTION_LENGTH);
    }
}
