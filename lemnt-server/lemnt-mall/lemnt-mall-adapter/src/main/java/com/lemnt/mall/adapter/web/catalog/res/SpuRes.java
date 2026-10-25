package com.lemnt.mall.adapter.web.catalog.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(description = "Spu响应")
public record SpuRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "编码") String code,
        @Schema(description = "名称") String name,
        @Schema(description = "分类编号") Long categoryId,
        @Schema(description = "品牌编号") Long brandId,
        @Schema(description = "描述") String description,
        @Schema(description = "图片URL") String image,
        @Schema(description = "状态") String status,
        @Schema(description = "SKU列表，分页列表时为空，详情包含完整SKU") java.util.List<SkuRes> skus)
        implements Serializable {}
