package com.lemnt.promotion.domain.product.gateway;
import com.lemnt.promotion.domain.product.model.*;
/** 营销上下文的只读商品防腐端口，不拥有商品持久化。 */
public interface ProductSkuGateway { ProductSku catalogSku(Long id); java.util.List<ProductSku> catalogSkus(Long spuId); java.util.List<ProductSku> skusBySpuIds(java.util.List<Long> ids); }
