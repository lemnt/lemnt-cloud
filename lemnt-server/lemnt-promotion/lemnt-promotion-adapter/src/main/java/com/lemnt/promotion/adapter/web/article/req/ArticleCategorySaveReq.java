package com.lemnt.promotion.adapter.web.article.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@Schema(description = "商城内容管理请求")
public record ArticleCategorySaveReq(
        @Schema(description = "分类名称") @NotBlank @Size(max = 100) String name,
        @Schema(description = "分类图片") @Size(max = 500) String picUrl,
        @Schema(description = "排序，值越大越靠前") @NotNull @Min(0) Integer sort,
        @Schema(description = "0启用，1停用") @NotNull @Min(0) @Max(1) Integer status)
        implements Serializable {}
