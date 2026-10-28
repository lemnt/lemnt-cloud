package com.lemnt.mall.domain.product.model;

/**
 * 品牌。
 */
public record Brand(
        Long id,
        String name,
        String picUrl,
        String description,
        Integer sort,
        String status
) {}
