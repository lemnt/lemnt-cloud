package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 商品SPU更新请求。
 */
@Schema(description = "ProductSpuUpdateReq")
public record ProductSpuUpdateReq(
        @Schema(description = "商品名称")
        @NotBlank
        @Size(max = 200)
        String name,
        @Schema(description = "分类编号")
        @NotNull
        @Positive
        Long categoryId,
        @Schema(description = "品牌编号")
        @NotNull
        @Positive
        Long brandId,
        @Schema(description = "商品描述")
        @Size(max = 65535)
        String description,
        @Schema(description = "主图URL")
        @Size(max = 500)
        String image,
        @Schema(description = "完整SKU集合，缺少的旧SKU将移除")
        @NotEmpty
        @Valid
        List<ProductSkuSaveReq> skus,
        @Schema(description = "配送方式，1 快递、2 自提；空集合为未配置")
        @NotNull @Size(max = 2)
        List<@NotNull @Min(1) @Max(2) Integer> deliveryTypes,
        @Schema(description = "快递运费模板编号")
        @Positive
        Long deliveryTemplateId,
        @Schema(description = "商品关键字")
        @NotBlank @Size(max = 255) String keyword,
        @Schema(description = "商品简介")
        @NotBlank @Size(max = 128) String introduction,
        @Schema(description = "商品轮播图，最多5张")
        @NotNull @Size(max = 5) List<@NotBlank @Size(max = 500) String> sliderPicUrls,
        @Schema(description = "排序，数值越大越靠前")
        @NotNull @Min(0) Integer sort
) implements Serializable {}
