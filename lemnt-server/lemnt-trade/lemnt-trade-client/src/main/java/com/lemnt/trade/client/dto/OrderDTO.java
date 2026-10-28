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
        LocalDateTime createTime,
        long usePoint,
        long givePoint,
        OrderDeliveryDTO delivery
) implements Serializable {
    /**
     * 创建交易订单实例。
     */
    public OrderDTO {
        items = items == null ? List.of() : List.copyOf(items);
    }

    /** 存量快照没有积分权益，按零积分订单读取。 */
    public OrderDTO(
            Long id,
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            String status,
            List<OrderItemDTO> items,
            LocalDateTime createTime) {
        this(id, orderNo, memberId, totalAmount, status, items, createTime, 0L, 0L);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public OrderDTO(
            Long id,
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            String status,
            List<OrderItemDTO> items,
            LocalDateTime createTime,
            long usePoint,
            long givePoint) {
        this(id, orderNo, memberId, totalAmount, status, items, createTime, usePoint, givePoint, null);
    }
}
