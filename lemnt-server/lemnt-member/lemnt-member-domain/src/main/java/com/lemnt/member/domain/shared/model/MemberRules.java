package com.lemnt.member.domain.shared.model;

import com.lemnt.common.core.exception.ServiceException;
import java.nio.charset.StandardCharsets;

/**
 * 会员规则。
 */
public final class MemberRules {
    private MemberRules() {}

    /**
     * 编号。
     */
    public static Long id(Long id) {
        if (id == null || id <= 0) {
            throw new ServiceException("编号不合法", 400);
        }
        return id;
    }

    /**
     * 文本。
     */
    public static String text(String value, int max) {
        if (value == null || value.isBlank() || value.length() > max) {
            throw new ServiceException("文本为空或超长", 400);
        }
        return value.trim();
    }

    /**
     * 可选。
     */
    public static String optional(String value, int max) {
        return value == null || value.isBlank() ? null : text(value, max);
    }

    /**
     * 手机号。
     */
    public static String mobile(String value) {
        String mobile = text(value, 24);
        if (!mobile.matches("[+]?[0-9]{7,20}")) {
            throw new ServiceException("手机号不合法", 400);
        }
        return mobile;
    }

    /**
     * 更新会员密码凭证。
     */
    public static void password(String value) {
        if (value == null
                || value.length() < 10
                || value.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ServiceException("密码须至少10个字符且不超过72字节", 400);
        }
    }

    /**
     * 转换分页查询或分页响应。
     */
    public static void page(int page, int size) {
        if (page < 1 || page > 1000000 || size < 1 || size > 100) {
            throw new ServiceException("分页范围不合法", 400);
        }
    }
}
