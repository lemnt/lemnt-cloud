package com.lemnt.product.client.event;

import java.io.Serializable;

/**
 * 原商品预占事实；活动消费方不能再次扣减商品库存。
 */
public record StockPreReservedEvent(String reservationNo, Long skuId, int quantity)
    implements Serializable {
}
