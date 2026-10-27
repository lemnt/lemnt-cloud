package com.lemnt.mall.adapter.web.catalog.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 分类保存请求。
 */
@Schema(description = "MallCategoryUpdateReq")
public record MallCategoryUpdateReq(
        @Schema(description = "父分类编号，根分类为null")
        @Positive
        Long parentId,
        @Schema(description = "分类名称")
        @NotBlank
        @Size(max = 100)
        String name,
        @Schema(description = "分类图片URL")
        @Size(max = 500)
        String picUrl,
        @Schema(description = "显示排序")
        @NotNull
        @Min(0)
        Integer sort,
        @Schema(description = "ENABLED或DISABLED")
        @NotBlank
        @Pattern(regexp = "ENABLED|DISABLED")
        String status
) implements Serializable {}
