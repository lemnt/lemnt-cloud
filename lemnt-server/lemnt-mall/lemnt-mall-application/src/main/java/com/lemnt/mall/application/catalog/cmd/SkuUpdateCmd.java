package com.lemnt.mall.application.catalog.cmd;

import java.math.BigDecimal;

/**
 * 商品SKU更新命令。
 */
public record SkuUpdateCmd(
        Long id,
        String name,
        BigDecimal price,
        String image
) implements java.io.Serializable {}
