package com.lemnt.mall.adapter.app.settlement.res;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 结算明细响应。
 */
@Schema(description = "CheckoutItem响应")
public record SettlementItemRes(
        @Schema(description = "SKU编号，沿用现有商品ID")
        Long productId,
        @Schema(description = "SKU编码，不可售商品可省略")
        String productCode,
        @Schema(description = "商品名称快照")
        String productName,
        @Schema(description = "商品单价，元，JSON字符串")
        BigDecimal unitPrice,
        @Schema(description = "商品数量")
        Integer quantity,
        @Schema(description = "商品行原金额，元")
        BigDecimal lineAmount,
        @Schema(description = "商品行实付金额，含分摊运费，元")
        BigDecimal payAmount,
        @Schema(description = "运费，元")
        BigDecimal deliveryPrice,
        @Schema(description = "商品SPU编号，历史未记录时为空")
        Long spuId,
        @Schema(description = "下单时商品SPU名称")
        String spuName,
        @Schema(description = "下单时规格名称和值，历史未记录与空规格区分")
        List<Property> properties,
        @Schema(description = "下单时商品图片")
        String picUrl,
        @Schema(description = "真实订单明细主键，报价和旧快照未知为空") Long orderItemId,
        @Schema(description = "订单项是否已评价，旧记录未知为空") Boolean commentStatus
) implements Serializable {
    /** 兼容报价/历史快照，订单项和评价元数据由持久化后读取。 */
    public SettlementItemRes(
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
            List<Property> properties,
            String picUrl
    ) {
        this(productId, productCode, productName, unitPrice, quantity, lineAmount, payAmount, deliveryPrice, spuId, spuName, properties, picUrl, null, null);
    }

    public SettlementItemRes {
        properties = properties == null ? null : List.copyOf(properties);
    }

    /** 历史未保存商品元数据，保持为空，不查询现值补历史。 */
    public SettlementItemRes(
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
    public SettlementItemRes(
            Long productId,
            String productCode,
            String productName,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineAmount) {
        this(productId, productCode, productName, unitPrice, quantity, lineAmount, null, BigDecimal.ZERO.setScale(2));
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
