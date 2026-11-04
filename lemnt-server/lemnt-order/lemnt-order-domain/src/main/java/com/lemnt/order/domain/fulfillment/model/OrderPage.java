package com.lemnt.order.domain.fulfillment.model;

import java.util.List;

/**
 * 订单分页。
 */
public record OrderPage(List<OrderSummary> records, long total) {
}
