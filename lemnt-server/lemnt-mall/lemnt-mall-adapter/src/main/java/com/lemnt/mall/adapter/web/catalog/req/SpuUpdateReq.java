package com.lemnt.mall.adapter.web.catalog.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 商品SPU更新请求。
 */
@Schema(description = "SpuUpdateReq")
public record SpuUpdateReq(
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
        String image
) implements Serializable {}
