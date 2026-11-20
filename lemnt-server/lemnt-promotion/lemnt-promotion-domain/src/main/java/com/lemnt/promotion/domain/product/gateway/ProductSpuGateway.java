package com.lemnt.promotion.domain.product.gateway;

import com.lemnt.promotion.domain.product.model.*;

import java.util.List;

/**
 * 营销上下文的只读商品防腐端口，不拥有商品持久化。
 */
public interface ProductSpuGateway {
    ProductSpu findSpu(Long id);

    List<ProductSpu> findSpus(List<Long> ids);

    List<Long> existingSpuIds(List<Long> ids);
}
