package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 商品SKU响应。
 */
@Schema(description = "Sku响应")
public record ProductSkuRes(
        @Schema(description = "编号")
        Long id,
        @Schema(description = "所属SPU编号，存量独立商品为null")
        Long spuId,
        @Schema(description = "编码")
        String code,
        @Schema(description = "名称")
        String name,
        @Schema(description = "价格，元")
        BigDecimal price,
        @Schema(description = "可用库存")
        Integer stock,
        @Schema(description = "图片URL")
        String image,
        @Schema(description = "SKU规格映射")
        Map<String,
        String> properties,
        @Schema(description = "状态")
        String status,
        @Schema(description = "重量，千克")
        BigDecimal weight,
        @Schema(description = "体积，立方米")
        BigDecimal volume,
        @Schema(description = "规格编号到规格值编号，null表示历史未关联")
        Map<Long, Long> specValueIds
) implements Serializable {
    /** 旧调用未提供字典关联，不推断历史编号。 */
    public ProductSkuRes(Long id, Long spuId, String code, String name, BigDecimal price, Integer stock,
            String image, Map<String, String> properties, String status, BigDecimal weight, BigDecimal volume) {
        this(id, spuId, code, name, price, stock, image, properties, status, weight, volume, null);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public ProductSkuRes(
            Long id,
            Long spuId,
            String code,
            String name,
            BigDecimal price,
            Integer stock,
            String image,
            Map<String, String> properties,
            String status) {
        this(id, spuId, code, name, price, stock, image, properties, status, null, null);
    }
}
