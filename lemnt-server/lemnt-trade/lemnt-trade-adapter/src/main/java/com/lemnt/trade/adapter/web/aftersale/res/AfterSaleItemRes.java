package com.lemnt.trade.adapter.web.aftersale.res;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/** 可售后商品额度。 */
@Schema(description = "可售后商品额度")
public record AfterSaleItemRes(
        @Schema(description = "订单商品行ID")
        Long orderItemId,
        @Schema(description = "商品ID")
        Long productId,
        @Schema(description = "商品名称")
        String productName,
        @Schema(description = "购买数量")
        Integer quantity,
        @Schema(description = "剩余可售后数量")
        Integer refundableQuantity,
        @Schema(description = "剩余可退款金额，单位元")
        BigDecimal refundableAmount,
        @Schema(description = "商品SPU编号，历史未记录时为空")
        Long spuId,
        @Schema(description = "下单时商品SPU名称")
        String spuName,
        @Schema(description = "下单时规格名称和值，历史未记录与空规格区分")
        List<Property> properties,
        @Schema(description = "下单时商品图片")
        String picUrl
) implements Serializable {
    public AfterSaleItemRes {
        properties = properties == null ? null : List.copyOf(properties);
    }

    /** 历史未保存商品元数据，保持为空，不查询现值补历史。 */
    public AfterSaleItemRes(
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
    @Schema(description = "商品规格快照")
    public record Property(
            @Schema(description = "规格项编号，历史未关联时为空")
            Long propertyId,
            @Schema(description = "保存时规格名称")
            String propertyName,
            @Schema(description = "规格值编号，历史未关联时为空")
            Long valueId,
            @Schema(description = "保存时规格值名称")
            String valueName
    ) implements java.io.Serializable {}
}
