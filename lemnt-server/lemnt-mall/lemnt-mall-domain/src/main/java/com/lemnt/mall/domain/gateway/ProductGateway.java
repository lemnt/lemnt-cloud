package com.lemnt.mall.domain.gateway;

import com.lemnt.mall.domain.model.Product;

public interface ProductGateway {
    Product findById(Long id);

    Product findSellableById(Long id);

    java.util.List<Product> findSellableByIds(java.util.List<Long> ids);

    void cancelReservation(String reservationNo, Long productId, Integer quantity);

    Product findByCode(String code);

    Product save(Product product);

    boolean updateStatus(Long id, String status);

    boolean reserveStock(String reservationNo, Long productId, Integer quantity);

    void releaseStock(String reservationNo, Long productId);

    void confirmStock(String reservationNo, Long productId);
}
