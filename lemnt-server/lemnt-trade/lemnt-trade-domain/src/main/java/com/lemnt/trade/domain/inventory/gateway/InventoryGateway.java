package com.lemnt.trade.domain.inventory.gateway;

/**
 * 商品目录领域网关。
 */
public interface InventoryGateway {

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
