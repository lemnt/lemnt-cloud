package com.lemnt.mall.application.product.cmd;

/**
 * 商品商品SPU上架命令。
 */
public record ProductSpuShelfCmd(
        Long id,
        boolean onShelf
) implements java.io.Serializable {}
