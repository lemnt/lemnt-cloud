package com.lemnt.mall.domain.cart.model;

/** 购物车规则。 */
public final class CartRules {
    private CartRules() {}

    /** 校验业务编号。 */
    public static void id(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("编号不合法");
        }
    }

    /** 校验并规范业务文本。 */
    public static String text(String value, int maximum) {
        if (value == null || value.isBlank() || value.trim().length() > maximum) {
            throw new IllegalArgumentException("文本参数不合法");
        }
        return value.trim();
    }
}
