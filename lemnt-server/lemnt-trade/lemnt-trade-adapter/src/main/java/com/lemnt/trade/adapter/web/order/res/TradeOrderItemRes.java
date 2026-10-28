package com.lemnt.trade.adapter.web.order.res;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/** 订单商品快照响应。 */
@Schema(description = "订单商品快照响应")
public record TradeOrderItemRes(
        @Schema(description = "商品ID")
        Long productId,
        @Schema(description = "商品编码")
        String productCode,
        @Schema(description = "商品名称")
        String productName,
        @Schema(description = "商品单价，单位元")
        BigDecimal unitPrice,
        @Schema(description = "购买数量")
        Integer quantity,
        @Schema(description = "商品合计金额，单位元")
        BigDecimal lineAmount,
        @Schema(description = "商品SPU编号，历史未记录时为空")
        Long spuId,
        @Schema(description = "下单时商品SPU名称")
        String spuName,
        @Schema(description = "下单时规格名称和值，历史未记录与空规格区分")
        Map<String, String> properties,
        @Schema(description = "下单时商品图片")
        String picUrl
) implements Serializable {
    public TradeOrderItemRes {
        properties = properties == null ? null : Map.copyOf(properties);
    }

    /** 历史未保存商品元数据，保持为空，不查询现值补历史。 */
    public TradeOrderItemRes(
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

}
