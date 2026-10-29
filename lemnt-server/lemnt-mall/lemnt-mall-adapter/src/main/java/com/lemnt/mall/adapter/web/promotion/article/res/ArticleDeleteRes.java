package com.lemnt.mall.adapter.web.promotion.article.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 商城文章删除结果。 */
@Schema(description = "商城文章删除结果")
public record ArticleDeleteRes(
        @Schema(description = "success") boolean success
) {}
