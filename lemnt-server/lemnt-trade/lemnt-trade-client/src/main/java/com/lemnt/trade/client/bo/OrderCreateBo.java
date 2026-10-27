package com.lemnt.trade.client.bo;

import com.lemnt.trade.client.dto.OrderItemDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 商城提交的已确认订单快照，不接受调用方指定订单状态。 */
public record OrderCreateBo(
        String orderNo,
        Long memberId,
        BigDecimal totalAmount,
        List<OrderItemDTO> items
) implements Serializable {
    public OrderCreateBo {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
