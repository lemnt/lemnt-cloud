package com.lemnt.promotion.application.cart.cmd;

import java.io.Serializable;

/**
 * 购物车删除命令。
 */
public record CartDeleteCmd(Long memberId, Long productId) implements Serializable {}
