package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 品牌保存请求。
 */
@Schema(description = "BrandSaveReq")
public record BrandSaveReq(
        @Schema(description = "品牌名称")
        @NotBlank
        @Size(max = 100)
        String name,
        @Schema(description = "品牌图片URL")
        @Size(max = 500)
        String picUrl,
        @Schema(description = "品牌描述")
        @Size(max = 2000)
        String description,
        @Schema(description = "显示排序")
        @NotNull
        @Min(0)
        Integer sort,
        @Schema(description = "ENABLED或DISABLED")
        @NotBlank
        @Pattern(regexp = "ENABLED|DISABLED")
        String status
) implements Serializable {}
