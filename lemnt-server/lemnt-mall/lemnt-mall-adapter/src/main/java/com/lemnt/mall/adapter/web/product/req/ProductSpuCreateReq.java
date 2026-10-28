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
 * 商品SPU创建请求。
 */
@Schema(description = "ProductSpuCreateReq")
public record ProductSpuCreateReq(
        @Schema(description = "SPU编码")
        @NotBlank
        @Size(max = 64)
        String code,
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
        @Schema(description = "SKU列表，1至100项")
        @NotEmpty
        @Size(max = 100)
        List<@Valid
        @NotNull
        ProductSkuCreateReq> skus,
        @Schema(description = "配送方式，1 快递、2 自提；空集合为未配置")
        @NotNull @Size(max = 2)
        List<@NotNull @Min(1) @Max(2) Integer> deliveryTypes,
        @Schema(description = "快递运费模板编号")
        @Positive
        Long deliveryTemplateId
) implements Serializable {}
