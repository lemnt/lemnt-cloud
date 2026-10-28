package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/** 批量商品详情SKU记录。 */
@Schema(description = "批量商品详情SKU记录")
public record ProductSpuBatchSkuRes(
        @Schema(description = "SKU编号")
        Long id,
        @Schema(description = "SPU编号")
        Long spuId,
        @Schema(description = "SKU编码")
        String code,
        @Schema(description = "SKU名称")
        String name,
        @Schema(description = "单价，单位元")
        BigDecimal price,
        @Schema(description = "可用库存")
        Integer stock,
        @Schema(description = "图片地址")
        String image,
        @Schema(description = "规格快照")
        Map<String, String> properties,
        @Schema(description = "ON_SHELF或OFF_SHELF")
        String status,
        @Schema(description = "重量，千克")
        BigDecimal weight,
        @Schema(description = "体积，立方米")
        BigDecimal volume,
        @Schema(description = "规格编号到规格值编号，null表示历史未关联")
        Map<Long, Long> specValueIds
) implements Serializable {
    /** 旧调用未提供字典关联，不推断历史编号。 */
    public ProductSpuBatchSkuRes(Long id, Long spuId, String code, String name, BigDecimal price, Integer stock,
            String image, Map<String, String> properties, String status, BigDecimal weight, BigDecimal volume) {
        this(id, spuId, code, name, price, stock, image, properties, status, weight, volume, null);
    }


    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public ProductSpuBatchSkuRes(
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
