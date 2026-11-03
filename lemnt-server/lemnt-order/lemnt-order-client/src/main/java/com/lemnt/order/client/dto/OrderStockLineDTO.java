package com.lemnt.order.client.dto;

import java.io.Serializable;

/** 订单原SKU行及购买数量，用于确认或释放原预占。 */
public record OrderStockLineDTO(Long skuId, int quantity) implements Serializable {}
