package com.lemnt.mall.adapter.app.promotion.article.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 启用文章分类。 */
@Schema(description = "启用文章分类")
public record AppArticleCategoryListRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "分类名称") String name,
        @Schema(description = "图片地址") String picUrl
) {}
