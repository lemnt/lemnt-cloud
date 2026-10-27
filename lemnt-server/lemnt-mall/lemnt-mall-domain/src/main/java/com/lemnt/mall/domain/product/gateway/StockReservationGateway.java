package com.lemnt.mall.domain.product.gateway;

import com.lemnt.mall.domain.product.model.StockReservation;

/** StockReservation 表领域存储接口。 */
public interface StockReservationGateway {
    /** 锁定已存在的预占记录。 */
    StockReservation lock(String key, Long productId);

    /** 保存预占或终止标记。 */
    void insert(StockReservation value);

    /** 释放预占状态。 */
    boolean release(String key, Long productId);

    /** 确认预占状态。 */
    boolean confirm(String key, Long productId);
}
