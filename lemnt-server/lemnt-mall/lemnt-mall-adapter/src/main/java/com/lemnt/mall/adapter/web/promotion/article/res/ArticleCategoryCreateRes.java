package com.lemnt.mall.adapter.web.promotion.article.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 文章分类新增结果。 */
@Schema(description = "文章分类新增结果")
public record ArticleCategoryCreateRes(
        @Schema(description = "编号") Long id
) {}
