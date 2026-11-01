package com.lemnt.mall.domain.settlement.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 结算单：保存服务端确认的商品明细和结算金额，用于提交订单及重试。
 */
public record SettlementOrder(
        Long id,
        String orderNo,
        Long memberId,
        BigDecimal totalAmount,
        String status,
        List<SettlementItem> items,
        LocalDateTime createTime,
        long usePoint,
        long givePoint,
        SettlementDelivery delivery,
        Boolean commentStatus,
        LocalDateTime finishTime,
        Map<Long, Integer> giveCouponTemplateCounts
) implements Serializable {
    /** 兼容报价/历史快照，营销赠券由支付回调按订单快照发放。 */
    public SettlementOrder(
            Long id,
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            String status,
            List<SettlementItem> items,
            LocalDateTime createTime,
            long usePoint,
            long givePoint,
            SettlementDelivery delivery,
            Boolean commentStatus,
            LocalDateTime finishTime
    ) {
        this(id, orderNo, memberId, totalAmount, status, items, createTime, usePoint, givePoint, delivery,
                commentStatus, finishTime, Map.of());
    }

    /**
     * 创建结算单实例。
     */
    public SettlementOrder {
        items = items == null ? List.of() : List.copyOf(items);
        giveCouponTemplateCounts = giveCouponTemplateCounts == null ? Map.of()
                : Map.copyOf(giveCouponTemplateCounts);
    }

    /** 兼容报价/历史快照，订单项和评价元数据由持久化后读取。 */
    public SettlementOrder(
            Long id,
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            String status,
            List<SettlementItem> items,
            LocalDateTime createTime,
            long usePoint,
            long givePoint,
            SettlementDelivery delivery
    ) {
        this(id, orderNo, memberId, totalAmount, status, items, createTime, usePoint, givePoint, delivery,
                null, null);
    }

    /** 存量快照没有积分权益，按零积分订单读取。 */
    public SettlementOrder(Long id, String orderNo, Long memberId, BigDecimal totalAmount, String status,
        List<SettlementItem> items, LocalDateTime createTime
    ) {
        this(id, orderNo, memberId, totalAmount, status, items, createTime, 0L, 0L);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public SettlementOrder(
            Long id,
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            String status,
            List<SettlementItem> items,
            LocalDateTime createTime,
            long usePoint,
            long givePoint) {
        this(id, orderNo, memberId, totalAmount, status, items, createTime, usePoint, givePoint, null);
    }
}
