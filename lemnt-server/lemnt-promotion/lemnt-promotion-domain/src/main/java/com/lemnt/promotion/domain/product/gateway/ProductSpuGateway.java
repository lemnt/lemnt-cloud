package com.lemnt.promotion.domain.product.gateway;
import com.lemnt.promotion.domain.product.model.*;
/** 营销上下文的只读商品防腐端口，不拥有商品持久化。 */
public interface ProductSpuGateway { ProductSpu findSpu(Long id); java.util.List<ProductSpu> findSpus(java.util.List<Long> ids); java.util.List<Long> existingSpuIds(java.util.List<Long> ids); }
