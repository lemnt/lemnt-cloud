package com.lemnt.trade.application.fulfillment.dto;

import java.time.LocalDateTime;

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
        ShipmentDTO shipment,
        Boolean commentStatus,
        LocalDateTime finishTime
) implements java.io.Serializable {
    /** 兼容报价/历史快照，订单项和评价元数据由持久化后读取。 */
    public OrderDetailDTO(
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            String status,
            boolean inventoryConfirmed,
            boolean activeRefund,
            List<OrderItemDTO> items,
            ShipmentDTO shipment
    ) {
        this(orderNo, memberId, totalAmount, status, inventoryConfirmed, activeRefund, items, shipment, null, null);
    }
}
