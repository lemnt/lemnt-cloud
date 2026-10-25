package com.lemnt.mall.adapter.web.catalog.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(description = "Sku响应")
public record SkuRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "所属SPU编号，存量独立商品为null") Long spuId,
        @Schema(description = "编码") String code,
        @Schema(description = "名称") String name,
        @Schema(description = "价格，元") java.math.BigDecimal price,
        @Schema(description = "可用库存") Integer stock,
        @Schema(description = "图片URL") String image,
        @Schema(description = "SKU规格映射") java.util.Map<String, String> properties,
        @Schema(description = "状态") String status)
        implements Serializable {}
