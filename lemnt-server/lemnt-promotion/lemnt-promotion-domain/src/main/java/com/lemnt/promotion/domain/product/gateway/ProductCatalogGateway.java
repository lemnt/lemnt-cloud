package com.lemnt.promotion.domain.product.gateway;
import com.lemnt.promotion.domain.product.model.*;
/** 营销上下文的只读商品防腐端口，不拥有商品持久化。 */
public interface ProductCatalogGateway { ProductSnapshot findOnShelf(Long id); java.util.Map<Long,ProductSnapshot> findOnShelfBatch(java.util.List<Long> ids); }
