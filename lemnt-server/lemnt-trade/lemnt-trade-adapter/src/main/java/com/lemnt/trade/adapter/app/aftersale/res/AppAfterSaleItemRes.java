package com.lemnt.trade.adapter.app.aftersale.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 会员可售后商品额度。 */
@Schema(description = "会员可售后商品额度")
public record AppAfterSaleItemRes(
        @Schema(description = "订单行编号")
        Long orderItemId,
        @Schema(description = "商品编号")
        Long productId,
        @Schema(description = "商品名称")
        String productName,
        @Schema(description = "购买数量")
        Integer quantity,
        @Schema(description = "剩余可售后数量")
        Integer refundableQuantity,
        @Schema(description = "剩余可退金额，元")
        BigDecimal refundableAmount,
        @Schema(description = "SPU编号")
        Long spuId,
        @Schema(description = "SPU名称")
        String spuName,
        @Schema(description = "规格快照")
        List<Property> properties,
        @Schema(description = "商品图片")
        String picUrl
) implements Serializable {
    public AppAfterSaleItemRes {
        properties = properties == null ? null : List.copyOf(properties);
    }

    /** 保存时的规格编号和名称。 */
    @Schema(description = "规格快照")
    public record Property(
            Long propertyId,
            String propertyName,
            Long valueId,
            String valueName
    ) implements Serializable {}
}
