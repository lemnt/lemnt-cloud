package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 商品SPU响应。
 */
@Schema(description = "Spu响应")
public record ProductSpuDetailRes(
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
    public ProductSpuDetailRes(
            Long id,
            String code,
            String name,
            Long categoryId,
            Long brandId,
            String description,
            String image,
            String status,
            List<ProductSkuRes> skus) {
        this(id, code, name, categoryId, brandId, description, image, status, skus, List.of(), null, null, null, null, null);
    }
}
