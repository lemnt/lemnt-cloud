package com.lemnt.mall.adapter.web.promotion.article.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 文章分类启用选项。 */
@Schema(description = "文章分类启用选项")
public record ArticleCategoryEnabledListRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "分类名称") String name
) {}
