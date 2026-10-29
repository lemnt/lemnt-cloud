package com.lemnt.mall.adapter.web.promotion.article.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 商城文章修改结果。 */
@Schema(description = "商城文章修改结果")
public record ArticleUpdateRes(
        @Schema(description = "success") boolean success
) {}
