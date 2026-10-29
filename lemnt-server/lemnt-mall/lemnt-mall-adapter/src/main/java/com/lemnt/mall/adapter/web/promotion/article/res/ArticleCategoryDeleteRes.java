package com.lemnt.mall.adapter.web.promotion.article.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 文章分类删除结果。 */
@Schema(description = "文章分类删除结果")
public record ArticleCategoryDeleteRes(
        @Schema(description = "success") boolean success
) {}
