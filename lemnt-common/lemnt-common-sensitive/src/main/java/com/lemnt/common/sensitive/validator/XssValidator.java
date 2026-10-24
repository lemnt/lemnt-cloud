package com.lemnt.common.sensitive.validator;

import com.lemnt.common.sensitive.annotation.Xss;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 自定义xss校验注解实现
 * <p>
 * 使用 OWASP 风格的标签白名单策略，过滤 XSS 脚本标签，
 * 同时允许常见富文本标签通过。
 * </p>
 *
 * @author wangxx
 */
public class XssValidator implements ConstraintValidator<Xss, String> {

    /**
     * 允许的安全 HTML 标签白名单
     */
    private static final String[] SAFE_TAGS = {
            "b", "i", "u", "em", "strong", "a", "br", "p", "div",
            "span", "ul", "ol", "li", "table", "tr", "td", "th",
            "h1", "h2", "h3", "h4", "h5", "h6"
    };

    /**
     * 匹配任意 HTML 标签开始/结束
     */
    private static final Pattern HTML_TAG_PATTERN =
            Pattern.compile("<\\s*/?\\s*(\\w+)[^>]*>");

    /**
     * 匹配事件处理器属性如 onclick, onload 等
     */
    private static final Pattern EVENT_HANDLER_PATTERN =
            Pattern.compile("(?i)\\bon\\w+\\s*=");

    /**
     * 匹配 javascript: 协议
     */
    private static final Pattern JAVASCRIPT_PROTOCOL_PATTERN =
            Pattern.compile("(?i)javascript\\s*:");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return !containsXss(value);
    }

    /**
     * 检测是否包含 XSS 风险
     */
    public static boolean containsXss(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        // 检查事件处理器
        if (EVENT_HANDLER_PATTERN.matcher(value).find()) {
            return true;
        }

        // 检查 javascript: 协议
        if (JAVASCRIPT_PROTOCOL_PATTERN.matcher(value).find()) {
            return true;
        }

        // 提取所有标签名，检查是否在白名单外
        java.util.regex.Matcher matcher = HTML_TAG_PATTERN.matcher(value);
        while (matcher.find()) {
            String tagName = matcher.group(1).toLowerCase();
            boolean isSafe = false;
            for (String safeTag : SAFE_TAGS) {
                if (safeTag.equals(tagName)) {
                    isSafe = true;
                    break;
                }
            }
            if (!isSafe) {
                return true;
            }
        }

        return false;
    }
}
