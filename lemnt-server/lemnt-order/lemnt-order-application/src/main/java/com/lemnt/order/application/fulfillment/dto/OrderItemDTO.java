package com.lemnt.order.application.fulfillment.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

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
        List<Spec> specs,
        String picUrl,
        Long orderItemId,
        Boolean commentStatus)
        implements Serializable {
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
            List<Spec> specs,
            String picUrl) {
        this(
                productId,
                productCode,
                productName,
                unitPrice,
                quantity,
                lineAmount,
                spuId,
                spuName,
                specs,
                picUrl,
                null,
                null);
    }

    public OrderItemDTO {
        specs = specs == null ? null : List.copyOf(specs);
    }

    /** 历史未保存商品元数据，保持为空，不查询现值补历史。 */
    public OrderItemDTO(
            Long productId,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineAmount) {
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
    public record Spec(Long specId, String specName, Long specValueId, String specValueName)
            implements Serializable {}
}
