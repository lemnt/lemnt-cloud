package com.lemnt.trade.client.bo;

import com.lemnt.trade.client.dto.OrderDeliveryDTO;
import com.lemnt.trade.client.dto.OrderItemDTO;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 商城提交的已确认订单快照，不接受调用方指定订单状态。 */
public record OrderCreateBo(
        String orderNo,
        Long memberId,
        BigDecimal totalAmount,
        List<OrderItemDTO> items,
        long usePoint,
        long givePoint,
        OrderDeliveryDTO delivery,
        Map<Long, Integer> giveCouponTemplateCounts,
        BigDecimal originalAmount,
        BigDecimal discountAmount,
        BigDecimal deliveryOriginalPrice,
        BigDecimal deliveryDiscountPrice,
        BigDecimal adjustmentAmount,
        String pricingSnapshotStatus,
        Integer pricingSnapshotVersion,
        java.util.List<com.lemnt.trade.client.dto.OrderPromotionDTO> promotions)
        implements Serializable {
    /** 兼容未记录本批优惠与退款元数据的历史快照。 */
    public OrderCreateBo(
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            List<OrderItemDTO> items,
            long usePoint,
            long givePoint,
            OrderDeliveryDTO delivery,
            Map<Long, Integer> giveCouponTemplateCounts) {
        this(
                orderNo,
                memberId,
                totalAmount,
                items,
                usePoint,
                givePoint,
                delivery,
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

    public OrderCreateBo {
        promotions = promotions == null ? java.util.List.of() : java.util.List.copyOf(promotions);
        items = items == null ? List.of() : List.copyOf(items);
        giveCouponTemplateCounts =
                giveCouponTemplateCounts == null ? Map.of() : Map.copyOf(giveCouponTemplateCounts);
    }

    /** 兼容无营销赠券的订单快照。 */
    public OrderCreateBo(
            String orderNo,
            Long memberId,
            BigDecimal totalAmount,
            List<OrderItemDTO> items,
            long usePoint,
            long givePoint,
            OrderDeliveryDTO delivery) {
        this(orderNo, memberId, totalAmount, items, usePoint, givePoint, delivery, Map.of());
    }

    /** 存量快照没有积分权益，按零积分订单读取。 */
    public OrderCreateBo(
            String orderNo, Long memberId, BigDecimal totalAmount, List<OrderItemDTO> items) {
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
