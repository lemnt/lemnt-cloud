package com.lemnt.mall.domain.catalog.model;

/**
 * 目录筛选。
 */
public record CatalogFilter(
        String name,
        String status,
        int offset,
        int limit
) {}
