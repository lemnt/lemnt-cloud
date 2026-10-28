package com.lemnt.trade.application.order.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 订单详情响应。 */
public record TradeOrderDetailDTO(
        Long id,
        String orderNo,
        Long memberId,
        BigDecimal totalAmount,
        String status,
        List<TradeOrderItemDTO> items
) implements Serializable {
}
