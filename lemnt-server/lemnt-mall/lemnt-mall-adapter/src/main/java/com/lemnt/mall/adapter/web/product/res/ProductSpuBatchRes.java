package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/** 批量商品详情记录。 */
@Schema(description = "批量商品详情记录")
public record ProductSpuBatchRes(
        @Schema(description = "商品ID")
        Long id,
        @Schema(description = "商品编码")
        String code,
        @Schema(description = "商品名称")
        String name,
        @Schema(description = "分类ID")
        Long categoryId,
        @Schema(description = "品牌ID")
        Long brandId,
        @Schema(description = "商品说明")
        String description,
        @Schema(description = "商品图片")
        String image,
        @Schema(description = "商品状态")
        String status,
        @Schema(description = "SKU详情")
        List<ProductSpuBatchSkuRes> skus,
        @Schema(description = "配送方式，1 快递、2 自提")
        List<Integer> deliveryTypes,
        @Schema(description = "运费模板编号")
        Long deliveryTemplateId,
        @Schema(description = "商品关键字")
        String keyword,
        @Schema(description = "商品简介")
        String introduction,
        @Schema(description = "轮播图地址，按显示顺序返回")
        List<String> sliderPicUrls,
        @Schema(description = "排序，数值越大越靠前")
        Integer sort
) implements Serializable {

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public ProductSpuBatchRes(
            Long id,
            String code,
            String name,
            Long categoryId,
            Long brandId,
            String description,
            String image,
            String status,
            List<ProductSpuBatchSkuRes> skus) {
        this(id, code, name, categoryId, brandId, description, image, status, skus, List.of(), null, null, null, null, null);
    }
}
