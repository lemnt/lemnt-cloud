package com.lemnt.trade.domain.aftersale.model;

import java.util.List;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 商品售后及退货信息。 */
public record AfterSale(
        String refundNo,
        String orderNo,
        String requestNo,
        Long memberId,
        String paymentNo,
        Long orderItemId,
        Integer quantity,
        String type,
        BigDecimal amount,
        String reason,
        String status,
        String decisionReason,
        String returnCarrierCode,
        String returnTrackingNo,
        LocalDateTime requestedAt,
        LocalDateTime decidedAt,
        LocalDateTime returnShippedAt,
        LocalDateTime returnReceivedAt,
        Long id,
        Long orderId,
        LocalDateTime refundTime,
        Long auditUserId,
        Long skuId,
        Long spuId,
        String spuName,
        List<Property> properties,
        String picUrl
) implements Serializable {
    public AfterSale {
        properties = properties == null ? null : List.copyOf(properties);
    }

    /** 历史未保存商品元数据，保持为空，不查询现值补历史。 */
    public AfterSale(
            String refundNo,
            String orderNo,
            String requestNo,
            Long memberId,
            String paymentNo,
            Long orderItemId,
            Integer quantity,
            String type,
            BigDecimal amount,
            String reason,
            String status,
            String decisionReason,
            String returnCarrierCode,
            String returnTrackingNo,
            LocalDateTime requestedAt,
            LocalDateTime decidedAt,
            LocalDateTime returnShippedAt,
            LocalDateTime returnReceivedAt,
            Long id,
            Long orderId,
            LocalDateTime refundTime,
            Long auditUserId
    ) {
        this(
                refundNo,
                orderNo,
                requestNo,
                memberId,
                paymentNo,
                orderItemId,
                quantity,
                type,
                amount,
                reason,
                status,
                decisionReason,
                returnCarrierCode,
                returnTrackingNo,
                requestedAt,
                decidedAt,
                returnShippedAt,
                returnReceivedAt,
                id,
                orderId,
                refundTime,
                auditUserId,
                null,
                null,
                null,
                null,
                null);
    }

    /** 存量内部调用，身份由数据库写入后读取。 */
    public AfterSale(
        String refundNo,
        String orderNo,
        String requestNo,
        Long memberId,
        String paymentNo,
        Long orderItemId,
        Integer quantity,
        String type,
        BigDecimal amount,
        String reason,
        String status,
        String decisionReason,
        String returnCarrierCode,
        String returnTrackingNo,
        LocalDateTime requestedAt,
        LocalDateTime decidedAt,
        LocalDateTime returnShippedAt,
        LocalDateTime returnReceivedAt
    ) {
        this(refundNo, orderNo, requestNo, memberId, paymentNo, orderItemId, quantity, type, amount, reason, status, decisionReason, returnCarrierCode, returnTrackingNo, requestedAt, decidedAt, returnShippedAt, returnReceivedAt, null, null, null, null);
    }

    /** 保存时的规格编号和名称，历史缺失编号保持为空。 */
    public record Property(
            Long propertyId,
            String propertyName,
            Long valueId,
            String valueName
    ) implements java.io.Serializable {}
}
