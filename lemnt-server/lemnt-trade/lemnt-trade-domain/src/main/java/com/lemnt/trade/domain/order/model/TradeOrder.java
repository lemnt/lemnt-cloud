package com.lemnt.trade.domain.order.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 交易订单聚合根：订单金额和商品信息来自服务端快照。
 */
public record TradeOrder(
        Long id,
        String orderNo,
        Long memberId,
        BigDecimal totalAmount,
        String status,
        List<TradeOrderItem> items,
        LocalDateTime createTime,
        long usePoint,
        long givePoint,
        TradeOrderDelivery delivery,
        Boolean commentStatus,
        LocalDateTime finishTime,
        Map<Long, Integer> giveCouponTemplateCounts,
        BigDecimal originalAmount,
        BigDecimal discountAmount,
        BigDecimal deliveryOriginalPrice,
        BigDecimal deliveryDiscountPrice,
        BigDecimal adjustmentAmount,
        String pricingSnapshotStatus,
        Integer pricingSnapshotVersion,
        java.util.List<com.lemnt.trade.domain.promotion.model.OrderPromotion> promotions) {
    /** 兼容未记录本批优惠与退款元数据的历史快照。 */
    public TradeOrder(
            Long id,
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            String status,
            List<TradeOrderItem> items,
            LocalDateTime createTime,
            long usePoint,
            long givePoint,
            TradeOrderDelivery delivery,
            Boolean commentStatus,
            LocalDateTime finishTime,
            Map<Long, Integer> giveCouponTemplateCounts) {
        this(
                id,
                orderNo,
                memberId,
                totalAmount,
                status,
                items,
                createTime,
                usePoint,
                givePoint,
                delivery,
                commentStatus,
                finishTime,
                giveCouponTemplateCounts,
                null,
                null,
                null,
                null,
                null,
                "LEGACY_PARTIAL",
                0,
                java.util.List.of());
    }

    /** 兼容无营销赠券的订单快照。 */
    public TradeOrder(
            Long id,
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            String status,
            List<TradeOrderItem> items,
            LocalDateTime createTime,
            long usePoint,
            long givePoint,
            TradeOrderDelivery delivery,
            Boolean commentStatus,
            LocalDateTime finishTime) {
        this(
                id,
                orderNo,
                memberId,
                totalAmount,
                status,
                items,
                createTime,
                usePoint,
                givePoint,
                delivery,
                commentStatus,
                finishTime,
                Map.of());
    }

    /** 兼容报价/历史快照，订单项和评价元数据由持久化后读取。 */
    public TradeOrder(
            Long id,
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            String status,
            List<TradeOrderItem> items,
            LocalDateTime createTime,
            long usePoint,
            long givePoint,
            TradeOrderDelivery delivery) {
        this(
                id,
                orderNo,
                memberId,
                totalAmount,
                status,
                items,
                createTime,
                usePoint,
                givePoint,
                delivery,
                null,
                null,
                Map.of());
    }

    /**
     * 创建交易订单实例。
     */
    public TradeOrder {
        promotions = promotions == null ? List.of() : List.copyOf(promotions);
        pricingSnapshotStatus =
                pricingSnapshotStatus == null ? "LEGACY_PARTIAL" : pricingSnapshotStatus;
        pricingSnapshotVersion = pricingSnapshotVersion == null ? 0 : pricingSnapshotVersion;
        items = items == null ? List.of() : List.copyOf(items);
        giveCouponTemplateCounts =
                giveCouponTemplateCounts == null ? Map.of() : Map.copyOf(giveCouponTemplateCounts);
    }

    /**
     * 待处理支付。
     */
    public static TradeOrder pendingPayment(
            String orderNo, Long memberId, BigDecimal totalAmount, List<TradeOrderItem> items) {
        if (orderNo == null || orderNo.isBlank()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("会员ID不合法");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("订单明细不能为空");
        }
        if (totalAmount == null || totalAmount.signum() < 0) {
            throw new IllegalArgumentException("订单金额不能为负");
        }
        return new TradeOrder(null, orderNo, memberId, totalAmount, "PENDING_PAYMENT", items, null);
    }

    /** 存量快照没有积分权益，按零积分订单读取。 */
    public TradeOrder(
            Long id,
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            String status,
            List<TradeOrderItem> items,
            LocalDateTime createTime) {
        this(id, orderNo, memberId, totalAmount, status, items, createTime, 0L, 0L);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public TradeOrder(
            Long id,
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            String status,
            List<TradeOrderItem> items,
            LocalDateTime createTime,
            long usePoint,
            long givePoint) {
        this(
                id,
                orderNo,
                memberId,
                totalAmount,
                status,
                items,
                createTime,
                usePoint,
                givePoint,
                null);
    }
}
