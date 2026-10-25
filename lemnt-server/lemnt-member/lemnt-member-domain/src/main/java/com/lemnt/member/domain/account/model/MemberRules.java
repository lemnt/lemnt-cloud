package com.lemnt.member.domain.account.model;

public final class MemberRules {
    private MemberRules() {}

    public static Long id(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("编号不合法");
        return id;
    }

    public static String text(String value, int max) {
        if (value == null || value.isBlank() || value.length() > max)
            throw new IllegalArgumentException("文本为空或超长");
        return value.trim();
    }

    public static String optional(String value, int max) {
        return value == null || value.isBlank() ? null : text(value, max);
    }

    public static String mobile(String value) {
        String mobile = text(value, 24);
        if (!mobile.matches("[+]?[0-9]{7,20}")) throw new IllegalArgumentException("手机号不合法");
        return mobile;
    }

    public static void password(String value) {
        if (value == null
                || value.length() < 10
                || value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("密码须至少10个字符且不超过72字节");
    }

    public static void page(int page, int size) {
        if (page < 1 || page > 1000000 || size < 1 || size > 100)
            throw new IllegalArgumentException("分页范围不合法");
    }
}
