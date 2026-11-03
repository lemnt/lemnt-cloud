package com.lemnt.mall.application.cart.cmd;

import java.io.Serializable;

/**
 * 购物车保存命令。
 */
public record CartSaveCmd(Long memberId, Long productId, int quantity, boolean selected)
        implements Serializable {}
