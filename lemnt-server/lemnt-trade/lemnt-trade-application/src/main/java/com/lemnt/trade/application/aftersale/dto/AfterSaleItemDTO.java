package com.lemnt.trade.application.aftersale.dto;

import java.util.List;

import java.io.Serializable;
import java.math.BigDecimal;

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
        List<Property> properties,
        String picUrl
) implements Serializable {
    public AfterSaleItemDTO {
        properties = properties == null ? null : List.copyOf(properties);
    }

    /** 历史未保存商品元数据，保持为空，不查询现值补历史。 */
    public AfterSaleItemDTO(
            Long orderItemId,
            Long productId,
            String productName,
            Integer quantity,
            Integer refundableQuantity,
            BigDecimal refundableAmount
    ) {
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
    public record Property(
            Long propertyId,
            String propertyName,
            Long valueId,
            String valueName
    ) implements java.io.Serializable {}
}
