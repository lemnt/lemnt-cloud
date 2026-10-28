package com.lemnt.trade.application.aftersale.dto;

import java.util.Map;

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
        Map<String, String> properties,
        String picUrl
) implements Serializable {
    public AfterSaleItemDTO {
        properties = properties == null ? null : Map.copyOf(properties);
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

}
