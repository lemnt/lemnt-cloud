package com.lemnt.mall.domain.store.model;

/**
 * 门店筛选。
 */
public record StoreFilter(
        String name,
        Long categoryId,
        int offset,
        int limit
) {}
