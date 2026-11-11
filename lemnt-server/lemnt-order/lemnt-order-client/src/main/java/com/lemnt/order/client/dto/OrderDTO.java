package com.lemnt.order.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    OrderDeliveryDTO delivery,
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
    List<OrderPromotionDTO> promotions)
    implements Serializable {
    /**
     * 兼容未记录本批优惠与退款元数据的历史快照。
     */
    public OrderDTO(
        Long id,
        String orderNo,
        Long memberId,
        BigDecimal totalAmount,
        String status,
        List<OrderItemDTO> items,
        LocalDateTime createTime,
        long usePoint,
        long givePoint,
        OrderDeliveryDTO delivery,
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
            List.of());
    }

    /**
     * 兼容无营销赠券的订单快照。
     */
    public OrderDTO(
        Long id,
        String orderNo,
        Long memberId,
        BigDecimal totalAmount,
        String status,
        List<OrderItemDTO> items,
        LocalDateTime createTime,
        long usePoint,
        long givePoint,
        OrderDeliveryDTO delivery,
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

    /**
     * 兼容报价/历史快照，订单项和评价元数据由持久化后读取。
     */
    public OrderDTO(
        Long id,
        String orderNo,
        Long memberId,
        BigDecimal totalAmount,
        String status,
        List<OrderItemDTO> items,
        LocalDateTime createTime,
        long usePoint,
        long givePoint,
        OrderDeliveryDTO delivery) {
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
    public OrderDTO {
        promotions = promotions == null ? List.of() : List.copyOf(promotions);
        items = items == null ? List.of() : List.copyOf(items);
        giveCouponTemplateCounts =
            giveCouponTemplateCounts == null ? Map.of() : Map.copyOf(giveCouponTemplateCounts);
    }

    /**
     * 存量快照没有积分权益，按零积分订单读取。
     */
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

    /**
     * 历史快照缺少配送字段，读取时保留未配置状态。
     */
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
