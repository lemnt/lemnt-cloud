package com.lemnt.product.domain.product.gateway;

import com.lemnt.product.domain.product.model.ProductStockReservation;

/**
 * ProductStockReservation 表领域存储接口。
 */
public interface ProductStockReservationGateway {
    /**
     * 原订单同 SKU 已退款回补数量。
     */
    int returnedQuantity(String orderNo, Long productId);

    /**
     * 预占中的SKU不能删除，确认或释放后的历史记录保留。
     */
    boolean hasActive(Long productId);

    /**
     * 锁定已存在的预占记录。
     */
    ProductStockReservation lock(String key, Long productId);

    /**
     * 保存预占或终止标记。
     */
    void insert(ProductStockReservation value);

    /**
     * 释放预占状态。
     */
    boolean release(String key, Long productId);

    /**
     * 确认预占状态。
     */
    boolean confirm(String key, Long productId);

    /** 确认预占并记录销量已入账，须与销量更新同事务。 */
    boolean recordSale(String key, Long productId);
}
