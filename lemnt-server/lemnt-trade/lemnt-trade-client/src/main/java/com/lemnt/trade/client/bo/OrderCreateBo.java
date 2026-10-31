package com.lemnt.trade.client.bo;

import com.lemnt.trade.client.dto.OrderDeliveryDTO;
import com.lemnt.trade.client.dto.OrderItemDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 商城提交的已确认订单快照，不接受调用方指定订单状态。 */
public record OrderCreateBo(
        String orderNo,
        Long memberId,
        BigDecimal totalAmount,
        List<OrderItemDTO> items,
        long usePoint,
        long givePoint,
        OrderDeliveryDTO delivery
) implements Serializable {
    public OrderCreateBo {
        items = items == null ? List.of() : List.copyOf(items);
    }

    /** 存量快照没有积分权益，按零积分订单读取。 */
    public OrderCreateBo(String orderNo, Long memberId, BigDecimal totalAmount, List<OrderItemDTO> items) {
        this(orderNo, memberId, totalAmount, items, 0L, 0L);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public OrderCreateBo(
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            List<OrderItemDTO> items,
            long usePoint,
            long givePoint) {
        this(orderNo, memberId, totalAmount, items, usePoint, givePoint, null);
    }
}
