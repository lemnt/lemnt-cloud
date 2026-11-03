package com.lemnt.trade.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 交易订单明细。
 */
public record OrderItemDTO(
        Long productId,
        String productCode,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineAmount,
        BigDecimal payAmount,
        BigDecimal deliveryPrice,
        Long spuId,
        String spuName,
        List<Spec> specs,
        String picUrl,
        Long orderItemId,
        Boolean commentStatus,
        BigDecimal discountAmount,
        BigDecimal deliveryOriginalPrice,
        BigDecimal deliveryDiscountPrice,
        BigDecimal adjustmentAmount,
        Long usePoint,
        Long givePoint,
        Integer refundedQuantity)
        implements Serializable {
    /** 兼容未记录本批优惠与退款元数据的历史快照。 */
    public OrderItemDTO(
            Long productId,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineAmount,
            BigDecimal payAmount,
            BigDecimal deliveryPrice,
            Long spuId,
            String spuName,
            List<Spec> specs,
            String picUrl,
            Long orderItemId,
            Boolean commentStatus) {
        this(
                productId,
                productCode,
                productName,
                unitPrice,
                quantity,
                lineAmount,
                payAmount,
                deliveryPrice,
                spuId,
                spuName,
                specs,
                picUrl,
                orderItemId,
                commentStatus,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    /** 兼容报价/历史快照，订单项和评价元数据由持久化后读取。 */
    public OrderItemDTO(
            Long productId,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineAmount,
            BigDecimal payAmount,
            BigDecimal deliveryPrice,
            Long spuId,
            String spuName,
            List<Spec> specs,
            String picUrl) {
        this(
                productId,
                productCode,
                productName,
                unitPrice,
                quantity,
                lineAmount,
                payAmount,
                deliveryPrice,
                spuId,
                spuName,
                specs,
                picUrl,
                null,
                null);
    }

    /** 历史未保存商品元数据，保持为空，不查询现值补历史。 */
    public OrderItemDTO(
            Long productId,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineAmount,
            BigDecimal payAmount,
            BigDecimal deliveryPrice) {
        this(
                productId,
                productCode,
                productName,
                unitPrice,
                quantity,
                lineAmount,
                payAmount,
                deliveryPrice,
                null,
                null,
                null,
                null);
    }

    public OrderItemDTO {
        specs = specs == null ? null : List.copyOf(specs);
        deliveryPrice = deliveryPrice == null ? BigDecimal.ZERO.setScale(2) : deliveryPrice;
    }

    /** 兼容存量订单快照，未记录行实付时由原订单金额分摊。 */
    public OrderItemDTO(
            Long productId,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineAmount) {
        this(productId, productCode, productName, unitPrice, quantity, lineAmount, null);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public OrderItemDTO(
            Long productId,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineAmount,
            BigDecimal payAmount) {
        this(
                productId,
                productCode,
                productName,
                unitPrice,
                quantity,
                lineAmount,
                payAmount,
                BigDecimal.ZERO.setScale(2));
    }

    /** 保存时的规格编号和名称，历史缺失编号保持为空。 */
    public record Spec(Long specId, String specName, Long specValueId, String specValueName)
            implements java.io.Serializable {}
}
