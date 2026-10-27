package com.lemnt.mall.application.catalog.cmd;

/**
 * 商品SPU更新命令。
 */
public record SpuUpdateCmd(
        Long id,
        String name,
        Long categoryId,
        Long brandId,
        String description,
        String image
) implements java.io.Serializable {}
