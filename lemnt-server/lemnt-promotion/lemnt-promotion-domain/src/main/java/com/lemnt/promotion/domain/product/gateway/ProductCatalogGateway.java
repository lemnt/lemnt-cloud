package com.lemnt.promotion.domain.product.gateway;

import com.lemnt.promotion.domain.product.model.*;

import java.util.List;
import java.util.Map;

/** 营销上下文的只读商品防腐端口，不拥有商品持久化。 */
public interface ProductCatalogGateway {
    ProductSnapshot findOnShelf(Long id);

    Map<Long, ProductSnapshot> findOnShelfBatch(List<Long> ids);
}
