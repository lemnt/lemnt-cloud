package com.lemnt.order.application.aftersale.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 订单商品可售后额度。 */
public record AfterSaleItemDTO(
        Long orderItemId,
        Long productId,
        String productName,
        Integer quantity,
        Integer refundableQuantity,
        BigDecimal refundableAmount,
        Long spuId,
        String spuName,
        List<Spec> specs,
        String picUrl,
        Integer refundedQuantity)
        implements Serializable {
    /** 兼容未记录本批优惠与退款元数据的历史快照。 */
    public AfterSaleItemDTO(
            Long orderItemId,
            Long productId,
            String productName,
            Integer quantity,
            Integer refundableQuantity,
            BigDecimal refundableAmount,
            Long spuId,
            String spuName,
            List<Spec> specs,
            String picUrl) {
        this(
                orderItemId,
                productId,
                productName,
                quantity,
                refundableQuantity,
                refundableAmount,
                spuId,
                spuName,
                specs,
                picUrl,
                null);
    }

    public AfterSaleItemDTO {
        specs = specs == null ? null : List.copyOf(specs);
    }

    /** 历史未保存商品元数据，保持为空，不查询现值补历史。 */
    public AfterSaleItemDTO(
            Long orderItemId,
            Long productId,
            String productName,
            Integer quantity,
            Integer refundableQuantity,
            BigDecimal refundableAmount) {
        this(
                orderItemId,
                productId,
                productName,
                quantity,
                refundableQuantity,
                refundableAmount,
                null,
                null,
                null,
                null);
    }

    /** 保存时的规格编号和名称，历史缺失编号保持为空。 */
    public record Spec(Long specId, String specName, Long specValueId, String specValueName)
            implements Serializable {}
}
