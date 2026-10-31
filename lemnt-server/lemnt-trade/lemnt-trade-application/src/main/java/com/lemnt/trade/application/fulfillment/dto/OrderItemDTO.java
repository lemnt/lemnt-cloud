package com.lemnt.trade.application.fulfillment.dto;

import java.util.List;

import java.math.BigDecimal;

/**
 * 结算明细数据传输对象。
 */
public record OrderItemDTO(
        Long productId,
        String productCode,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineAmount,
        Long spuId,
        String spuName,
        List<Property> properties,
        String picUrl,
        Long orderItemId,
        Boolean commentStatus
) implements java.io.Serializable {
    /** 兼容报价/历史快照，订单项和评价元数据由持久化后读取。 */
    public OrderItemDTO(
            Long productId,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineAmount,
            Long spuId,
            String spuName,
            List<Property> properties,
            String picUrl
    ) {
        this(productId, productCode, productName, unitPrice, quantity, lineAmount, spuId, spuName, properties, picUrl, null, null);
    }

    public OrderItemDTO {
        properties = properties == null ? null : List.copyOf(properties);
    }

    /** 历史未保存商品元数据，保持为空，不查询现值补历史。 */
    public OrderItemDTO(
            Long productId,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineAmount
    ) {
        this(
                productId,
                productCode,
                productName,
                unitPrice,
                quantity,
                lineAmount,
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
