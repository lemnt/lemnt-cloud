package com.lemnt.mall.adapter.web.promotion.article.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 文章分类分页响应。 */
@Schema(description = "文章分类分页响应")
public record ArticleCategoryPageRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "分类名称") String name,
        @Schema(description = "图片地址") String picUrl,
        @Schema(description = "状态：0启用1禁用") Integer status,
        @Schema(description = "排序") Integer sort,
        @Schema(description = "创建时间") java.time.LocalDateTime createTime
) {}
