package com.lemnt.mall.adapter.web.catalog.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Schema(description = "SpuCreateReq")
public record SpuCreateReq(
        @Schema(description = "SPU编码") @NotBlank @Size(max = 64) String code,
        @Schema(description = "商品名称") @NotBlank @Size(max = 200) String name,
        @Schema(description = "分类编号") @NotNull @Positive Long categoryId,
        @Schema(description = "品牌编号") @NotNull @Positive Long brandId,
        @Schema(description = "商品描述") @Size(max = 65535) String description,
        @Schema(description = "主图URL") @Size(max = 500) String image,
        @Schema(description = "SKU列表，1至100项") @NotEmpty @Size(max = 100)
                java.util.List<@jakarta.validation.Valid @NotNull SkuCreateReq> skus)
        implements Serializable {}
