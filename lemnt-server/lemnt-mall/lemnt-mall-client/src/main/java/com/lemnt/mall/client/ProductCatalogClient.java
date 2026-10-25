package com.lemnt.mall.client;

import com.lemnt.mall.client.dto.ProductSnapshotDTO;

public interface ProductCatalogClient {
    ProductSnapshotDTO findOnShelf(Long productId);

    java.util.List<ProductSnapshotDTO> findOnShelfBatch(java.util.List<Long> productIds);

    void cancelReservation(String reservationNo, Long productId, Integer quantity);

    boolean reserveStock(String reservationNo, Long productId, Integer quantity);

    void releaseStock(String reservationNo, Long productId);

    void confirmStock(String reservationNo, Long productId);
}
