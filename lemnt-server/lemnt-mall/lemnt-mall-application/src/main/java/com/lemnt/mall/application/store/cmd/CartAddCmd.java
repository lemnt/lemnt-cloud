package com.lemnt.mall.application.store.cmd;

/**
 * 购物车新增命令。
 */
public record CartAddCmd(
        Long memberId,
        Long productId,
        int quantity,
        String requestNo
) implements java.io.Serializable {}
