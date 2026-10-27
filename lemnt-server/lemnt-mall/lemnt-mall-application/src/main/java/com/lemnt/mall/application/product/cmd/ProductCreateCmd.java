package com.lemnt.mall.application.product.cmd;

import java.math.BigDecimal;

/**
 * 商品创建命令。
 */
public record ProductCreateCmd(
        String productCode,
        String name,
        String description,
        String mainImage,
        BigDecimal price,
        Integer stock
) implements java.io.Serializable {}
