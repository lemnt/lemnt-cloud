package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 商品SPU响应。
 */
@Schema(description = "Spu响应")
public record ProductSpuPageRes(
        @Schema(description = "编号")
        Long id,
        @Schema(description = "编码")
        String code,
        @Schema(description = "名称")
        String name,
        @Schema(description = "分类编号")
        Long categoryId,
        @Schema(description = "品牌编号")
        Long brandId,
        @Schema(description = "描述")
        String description,
        @Schema(description = "图片URL")
        String image,
        @Schema(description = "状态")
        String status,
        @Schema(description = "SKU列表，分页列表时为空，详情包含完整SKU")
        List<ProductSkuRes> skus,
        @Schema(description = "商品关键字")
        String keyword,
        @Schema(description = "商品简介")
        String introduction,
        @Schema(description = "轮播图地址，按显示顺序返回")
        List<String> sliderPicUrls,
        @Schema(description = "排序，数值越大越靠前")
        Integer sort
) implements Serializable {}
