package com.lemnt.mall.domain.cart.model;

/**
 * 购物车引用。
 */
public record CartReference(
        Long cartId,
        long cartVersion
) {}
