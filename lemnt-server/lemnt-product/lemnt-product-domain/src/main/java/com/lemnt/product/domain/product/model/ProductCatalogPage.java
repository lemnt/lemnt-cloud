package com.lemnt.product.domain.product.model;

import java.util.List;
import java.util.Map;

/**
 * 目录分页。
 */
public record ProductCatalogPage(
        List<ProductSpu> records, long total, Map<Long, ProductSkuTotals> skuTotals) {
    /** 兼容只读取商品目录的调用。 */
    public ProductCatalogPage(List<ProductSpu> records, long total) {
        this(records, total, Map.of());
    }
}
