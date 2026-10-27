package com.lemnt.trade.application.fulfillment.dto;

import com.lemnt.trade.application.fulfillment.dto.OrderItemDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单详情数据传输对象。
 */
public record OrderDetailDTO(
        String orderNo,
        Long memberId,
        BigDecimal totalAmount,
        String status,
        boolean inventoryConfirmed,
        boolean activeRefund,
        List<OrderItemDTO> items,
        ShipmentDTO shipment
) implements java.io.Serializable {}
