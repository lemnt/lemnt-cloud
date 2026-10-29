package com.lemnt.mall.adapter.app.promotion.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** App文章详情请求。 */
@Schema(description = "App文章详情请求")
public record AppArticleDetailReq(
        @Schema(description = "编号") @Positive Long id,
        @Schema(description = "文章标题") @Size(max = 255) String title
) {}
