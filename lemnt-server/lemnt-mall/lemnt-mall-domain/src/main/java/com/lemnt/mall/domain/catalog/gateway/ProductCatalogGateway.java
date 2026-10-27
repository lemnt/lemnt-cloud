package com.lemnt.mall.domain.catalog.gateway;

import com.lemnt.mall.domain.catalog.model.ProductSnapshot;

import java.util.List;
import java.util.Map;

/**
 * 商品目录领域网关。
 */
public interface ProductCatalogGateway {
    /**
     * 查找上架状态。
     */
    ProductSnapshot findOnShelf(Long productId);

    /**
     * 查找批量上架状态。
     */
    Map<Long, ProductSnapshot> findOnShelfBatch(List<Long> productIds);

}
