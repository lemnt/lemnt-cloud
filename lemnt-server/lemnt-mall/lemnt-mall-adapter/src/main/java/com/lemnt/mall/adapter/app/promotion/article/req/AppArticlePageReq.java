package com.lemnt.mall.adapter.app.promotion.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** App文章分页请求。 */
@Schema(description = "App文章分页请求")
public record AppArticlePageReq(
        @Schema(description = "文章分类编号") @Positive Long categoryId,
        @Schema(description = "页码，默认1") @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页数量，默认20最多100") @Min(1) @Max(100) Integer pageSize
) {}
