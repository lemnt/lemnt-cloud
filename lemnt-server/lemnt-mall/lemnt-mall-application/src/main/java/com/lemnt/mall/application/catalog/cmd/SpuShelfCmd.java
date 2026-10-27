package com.lemnt.mall.application.catalog.cmd;

/**
 * 商品商品SPU上架命令。
 */
public record SpuShelfCmd(
        Long id,
        boolean onShelf
) implements java.io.Serializable {}
