package com.lemnt.mall.adapter.app.promotion.article.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 文章浏览计数结果。 */
@Schema(description = "文章浏览计数结果")
public record AppArticleBrowseCountRes(
        @Schema(description = "success") boolean success
) {}
