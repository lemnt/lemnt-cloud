package com.lemnt.mall.domain.product.model;

import java.util.List;

/**
 * 目录分页。
 */
public record ProductCatalogPage(
        List<ProductSpu> records,
        long total
) {}
