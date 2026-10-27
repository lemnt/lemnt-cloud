package com.lemnt.trade.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易订单跨服务快照。
 */
public record OrderDTO(
        Long id,
        String orderNo,
        Long memberId,
        BigDecimal totalAmount,
        String status,
        List<OrderItemDTO> items,
        LocalDateTime createTime
) implements Serializable {
    /**
     * 创建交易订单实例。
     */
    public OrderDTO {
        items = items == null ? List.of() : List.copyOf(items);
    }

}
