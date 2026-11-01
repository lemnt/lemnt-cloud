package com.lemnt.trade.application.fulfillment.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 会员订单详情：在履约详情基础上补充下单时间。 */
public record MemberOrderDetailDTO(
        String orderNo,
        String status,
        BigDecimal totalAmount,
        LocalDateTime createTime,
        boolean activeRefund,
        List<OrderItemDTO> items,
        ShipmentDTO shipment,
        Boolean commentStatus,
        LocalDateTime finishTime,
        String pickUpStoreName,
        String pickUpStorePhone,
        String pickUpStoreAddress,
        String pickUpVerifyCode
) implements Serializable {
    public MemberOrderDetailDTO {
        items = items == null ? List.of() : List.copyOf(items);
    }

    /** 兼容普通快递订单。 */
    public MemberOrderDetailDTO(String orderNo, String status, java.math.BigDecimal totalAmount,
            LocalDateTime createTime, boolean activeRefund, List<OrderItemDTO> items, ShipmentDTO shipment,
            Boolean commentStatus, LocalDateTime finishTime) {
        this(orderNo, status, totalAmount, createTime, activeRefund, items, shipment, commentStatus,
                finishTime, null, null, null, null);
    }
}
