package com.lemnt.mall.application.product.cmd;

/**
 * 品牌保存命令。
 */
public record BrandSaveCmd(
        Long id,
        String name,
        String picUrl,
        String description,
        Integer sort,
        String status
) implements java.io.Serializable {}
