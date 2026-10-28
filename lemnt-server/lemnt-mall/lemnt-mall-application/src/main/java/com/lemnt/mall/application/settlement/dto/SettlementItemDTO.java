package com.lemnt.mall.application.settlement.dto;

import java.util.Map;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 结算明细数据传输对象。
 */
public record SettlementItemDTO(
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
        Map<String, String> properties,
        String picUrl
) implements Serializable {
    public SettlementItemDTO {
        properties = properties == null ? null : Map.copyOf(properties);
    }

    /** 历史未保存商品元数据，保持为空，不查询现值补历史。 */
    public SettlementItemDTO(
            Long productId,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineAmount,
            BigDecimal payAmount,
            BigDecimal deliveryPrice
    ) {
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

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public SettlementItemDTO(
            Long productId,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineAmount) {
        this(productId, productCode, productName, unitPrice, quantity, lineAmount, null, BigDecimal.ZERO.setScale(2));
    }
}
