package com.lemnt.product.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品SKU响应。
 */
@Schema(description = "Sku响应")
public record ProductSkuRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "所属SPU编号，存量独立商品为null") Long spuId,
        @Schema(description = "编码") String code,
        @Schema(description = "名称") String name,
        @Schema(description = "价格，元") BigDecimal price,
        @Schema(description = "可用库存") Integer stock,
        @Schema(description = "图片URL") String image,
        @Schema(description = "SKU规格列表") List<Spec> specs,
        @Schema(description = "状态") String status,
        @Schema(description = "重量，千克") BigDecimal weight,
        @Schema(description = "体积，立方米") BigDecimal volume,
        @Schema(description = "划线价，单位元，历史未知为null") BigDecimal marketPrice,
        @Schema(description = "成本价，单位元，历史未知为null") BigDecimal costPrice,
        @Schema(description = "商品条码，与SKU编码独立") String barCode,
        @Schema(description = "本系统SKU支付确认累计件数，售后不冲减") Long salesCount,
        @Schema(description = "每件一级固定佣金，单位元") BigDecimal firstBrokeragePrice,
        @Schema(description = "每件二级固定佣金，单位元") BigDecimal secondBrokeragePrice)
        implements Serializable {
    /** 旧调用没有商品补充资料，保留未知值。 */
    public ProductSkuRes(
            Long id,
            Long spuId,
            String code,
            String name,
            BigDecimal price,
            Integer stock,
            String image,
            List<Spec> specs,
            String status,
            BigDecimal weight,
            BigDecimal volume) {
        this(
                id, spuId, code, name, price, stock, image, specs, status, weight, volume, null,
                null, null, null, null, null);
    }

    /**
     * 缺少配送字段的规格列表。
     */
    public ProductSkuRes(
            Long id,
            Long spuId,
            String code,
            String name,
            BigDecimal price,
            Integer stock,
            String image,
            List<Spec> specs,
            String status) {
        this(id, spuId, code, name, price, stock, image, specs, status, null, null);
    }

    /**
     * 编号与名称保持一一对应。
     */
    @Schema(description = "商品规格")
    public record Spec(
            @Schema(description = "规格项编号") Long specId,
            @Schema(description = "规格名称，写入时可省略") String specName,
            @Schema(description = "规格值编号") Long specValueId,
            @Schema(description = "规格值名称，写入时可省略") String specValueName)
            implements Serializable {}
}
