package com.lemnt.trade.application.payment.support;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.trade.domain.payment.model.PaymentChannel;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.regex.Pattern;

/** 支付应用与渠道配置的入参校验，统一避免各执行器重复。 */
public final class PayConfigRules {
    private static final Pattern APP_KEY = Pattern.compile("^[A-Za-z0-9_-]{4,64}$");

    private PayConfigRules() {
    }

    public static void page(int pageNum, int pageSize) {
        if (pageNum < 1 || pageNum > 1000000 || pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("分页参数不合法");
        }
    }

    public static void status(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new IllegalArgumentException("状态只能为0启用或1停用");
        }
    }

    public static String appKey(String value) {
        String text = required(value, 64, "应用标识");
        if (!APP_KEY.matcher(text).matches()) {
            throw new IllegalArgumentException("应用标识须为4-64位字母、数字、下划线或中划线");
        }
        return text;
    }

    public static String required(String value, int max, String field) {
        if (value == null || value.isBlank() || value.trim().length() > max) {
            throw new IllegalArgumentException(field + "不能为空且不超过" + max + "字");
        }
        return value.trim();
    }

    public static String optional(String value, int max, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String text = value.trim();
        if (text.length() > max) {
            throw new IllegalArgumentException(field + "不超过" + max + "字");
        }
        return text;
    }

    public static String url(String value, String field) {
        String text = optional(value, 255, field);
        if (text != null && !(text.startsWith("https://") || text.startsWith("http://"))) {
            throw new IllegalArgumentException(field + "须为http或https地址");
        }
        return text;
    }

    /** 只接受当前系统声明支持的渠道编码，避免存入无法识别的渠道。 */
    public static String channelCode(String value) {
        String text = required(value, 32, "渠道编码");
        if (Arrays.stream(PaymentChannel.values()).noneMatch(channel -> channel.name().equals(text))) {
            throw new IllegalArgumentException("不支持的渠道编码");
        }
        return text;
    }

    public static BigDecimal feeRate(BigDecimal value) {
        if (value == null) {
            return null;
        }
        if (value.signum() < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("渠道费率须在0至100之间");
        }
        return value.setScale(2, java.math.RoundingMode.DOWN);
    }

    /** 渠道配置必须为 JSON 对象文本，保存为不透明字符串，不在此处解析字段含义。 */
    public static String config(String value) {
        String text = optional(value, 4000, "渠道配置");
        if (text == null) {
            return null;
        }
        try {
            JsonUtils.toMap(text);
        } catch (RuntimeException error) {
            throw new IllegalArgumentException("渠道配置必须是合法JSON对象");
        }
        return text;
    }
}
