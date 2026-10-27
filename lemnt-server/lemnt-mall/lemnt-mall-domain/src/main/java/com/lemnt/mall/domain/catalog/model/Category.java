package com.lemnt.mall.domain.catalog.model;

/**
 * 分类。
 */
public record Category(
        Long id,
        Long parentId,
        String name,
        String picUrl,
        Integer sort,
        String status
) {}
