package com.lemnt.trade.application.order.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 订单商品快照响应。 */
public record TradeOrderItemDTO(
        Long productId,
        String productCode,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineAmount,
        Long spuId,
        String spuName,
        List<Spec> specs,
        String picUrl)
        implements Serializable {
    public TradeOrderItemDTO {
        specs = specs == null ? null : List.copyOf(specs);
    }

    /** 历史未保存商品元数据，保持为空，不查询现值补历史。 */
    public TradeOrderItemDTO(
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
            implements java.io.Serializable {}
}
