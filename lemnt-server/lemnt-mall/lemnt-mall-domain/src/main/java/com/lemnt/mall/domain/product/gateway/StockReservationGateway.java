package com.lemnt.mall.domain.product.gateway;

import com.lemnt.mall.domain.product.model.StockReservation;

/** StockReservation 表领域存储接口。 */
public interface StockReservationGateway {
    /** 原订单同 SKU 已退款回补数量。 */
    int returnedQuantity(String orderNo, Long productId);

    /** 预占中的SKU不能删除，确认或释放后的历史记录保留。 */
    boolean hasActive(Long productId);

    /** 锁定已存在的预占记录。 */
    StockReservation lock(String key, Long productId);

    /** 保存预占或终止标记。 */
    void insert(StockReservation value);

    /** 释放预占状态。 */
    boolean release(String key, Long productId);

    /** 确认预占状态。 */
    boolean confirm(String key, Long productId);
}
