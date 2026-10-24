package com.lemnt.trade.domain.gateway;

import com.lemnt.trade.domain.model.ProductSnapshot;

public interface ProductCatalogGateway {
    ProductSnapshot findOnShelf(Long productId);
    boolean reserveStock(String reservationNo, Long productId, Integer quantity);
    void releaseStock(String reservationNo, Long productId);
    void confirmStock(String reservationNo, Long productId);
}