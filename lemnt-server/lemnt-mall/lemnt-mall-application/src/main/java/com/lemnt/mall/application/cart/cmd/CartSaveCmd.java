package com.lemnt.mall.application.cart.cmd;

/**
 * 购物车保存命令。
 */
public record CartSaveCmd(
        Long memberId,
        Long productId,
        int quantity,
        boolean selected
) implements java.io.Serializable {}
