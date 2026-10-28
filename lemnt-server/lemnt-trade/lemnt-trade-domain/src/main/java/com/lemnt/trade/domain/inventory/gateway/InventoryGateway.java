package com.lemnt.trade.domain.inventory.gateway;

/**
 * 商品目录领域网关。
 */
public interface InventoryGateway {

    /**
     * 预留库存。
     */
    boolean reserveStock(String reservationNo, Long productId, Integer quantity);

    /**
     * 确认库存。
     */
    void confirmStock(String reservationNo, Long productId);
}
