package com.lemnt.mall.adapter.app.promotion.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** App文章推荐请求。 */
@Schema(description = "App文章推荐请求")
public record AppArticleRecommendedListReq(
        @Schema(description = "是否热门") Boolean recommendHot,
        @Schema(description = "是否轮播") Boolean recommendBanner
) {}
