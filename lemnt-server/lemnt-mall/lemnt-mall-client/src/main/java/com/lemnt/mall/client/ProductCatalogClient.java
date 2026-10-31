package com.lemnt.mall.client;

import com.lemnt.mall.client.dto.ProductSnapshotDTO;

import java.util.List;

/**
 * 商品目录客户端。
 */
public interface ProductCatalogClient {
    /**
     * 查找上架状态。
     */
    ProductSnapshotDTO findOnShelf(Long productId);

    /**
     * 查找批量上架状态。
     */
    List<ProductSnapshotDTO> findOnShelfBatch(List<Long> productIds);

    /**
     * 取消预留。
     */
    void cancelReservation(String reservationNo, Long productId, Integer quantity);

    /**
     * 预留库存。
     */
    boolean reserveStock(String reservationNo, Long productId, Integer quantity);

    /**
     * 释放库存。
     */
    void releaseStock(String reservationNo, Long productId);

    /**
     * 确认库存。
     */
    void confirmStock(String reservationNo, Long productId);
}
