package com.lemnt.mall.adapter.web.promotion.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** 文章分类新增请求。 */
@Schema(description = "文章分类新增请求")
public record ArticleCategoryCreateReq(
        @Schema(description = "分类名称") @Size(max = 128) @NotBlank String name,
        @Schema(description = "图片地址") @Size(max = 1024) String picUrl,
        @Schema(description = "状态：0启用1禁用") @Min(0) @Max(1) @NotNull Integer status,
        @Schema(description = "排序") @Min(0) @NotNull Integer sort
) {}
