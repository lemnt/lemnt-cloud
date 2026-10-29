package com.lemnt.mall.adapter.web.promotion.article.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 商城文章新增结果。 */
@Schema(description = "商城文章新增结果")
public record ArticleCreateRes(
        @Schema(description = "编号") Long id
) {}
