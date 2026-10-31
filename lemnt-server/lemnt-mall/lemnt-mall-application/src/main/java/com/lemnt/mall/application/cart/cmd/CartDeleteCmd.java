package com.lemnt.mall.application.cart.cmd;

/**
 * 购物车删除命令。
 */
public record CartDeleteCmd(
        Long memberId,
        Long productId
) implements java.io.Serializable {}
