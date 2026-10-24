package com.lemnt.trade.domain.gateway;
import com.lemnt.trade.domain.model.ProductSnapshot;
public interface ProductCatalogGateway { ProductSnapshot findOnShelf(Long productId); boolean reserveStock(Long productId,Integer quantity); void releaseStock(Long productId,Integer quantity); }