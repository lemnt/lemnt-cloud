package com.lemnt.trade.application.fulfillment.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 会员订单列表项：含商品明细，供订单中心展示缩略图与数量。 */
public record MemberOrderSummaryDTO(
        String orderNo,
        String status,
        BigDecimal totalAmount,
        LocalDateTime createTime,
        Boolean commentStatus,
        LocalDateTime finishTime,
        boolean activeRefund,
        List<OrderItemDTO> items
) implements Serializable {
    public MemberOrderSummaryDTO {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
