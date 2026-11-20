package com.lemnt.order.application.fulfillment.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 订单分页数据传输对象。
 */
public record OrderPageDTO(List<OrderSummaryDTO> records, long total) implements Serializable {
}
