package com.lemnt.trade.adapter.app.order.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 会员订单商品明细快照。 */
@Schema(description = "会员订单商品明细")
public record AppOrderItemRes(
        @Schema(description = "商品编号")
        Long productId,
        @Schema(description = "商品编码")
        String productCode,
        @Schema(description = "商品名称")
        String productName,
        @Schema(description = "单价，元")
        BigDecimal unitPrice,
        @Schema(description = "数量")
        Integer quantity,
        @Schema(description = "行金额，元")
        BigDecimal lineAmount,
        @Schema(description = "SPU编号")
        Long spuId,
        @Schema(description = "SPU名称")
        String spuName,
        @Schema(description = "规格快照")
        List<Property> properties,
        @Schema(description = "商品图片")
        String picUrl,
        @Schema(description = "订单行编号")
        Long orderItemId,
        @Schema(description = "该行是否已评价，历史未知为空")
        Boolean commentStatus
) implements Serializable {
    public AppOrderItemRes {
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
