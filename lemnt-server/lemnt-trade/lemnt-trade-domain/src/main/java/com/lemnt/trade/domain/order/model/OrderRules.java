package com.lemnt.trade.domain.order.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** 订单标识和幂等内容校验。 */
public final class OrderRules {
    private OrderRules() {}

    /** 校验正整数标识。 */
    public static void id(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("编号不合法");
        }
    }

    /** 校验订单文本参数。 */
    public static String text(String value, int max) {
        if (value == null || value.isBlank() || value.trim().length() > max) {
            throw new IllegalArgumentException("文本参数不合法");
        }
        return value.trim();
    }

    /** 生成请求内容摘要。 */
    public static String fingerprint(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
