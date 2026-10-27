package com.lemnt.mall.domain.catalog.model;

import java.util.List;

/**
 * 目录分页。
 */
public record CatalogPage(
        List<CatalogSpu> records,
        long total
) {}
