package com.lemnt.promotion.adapter.web.article.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "商城内容查询响应")
public record ArticleCategoryRes(
        @Schema(description = "记录编号") Long id,
        @Schema(description = "分类名称") String name,
        @Schema(description = "分类图片") String picUrl,
        @Schema(description = "排序，值越大越靠前") Integer sort,
        @Schema(description = "0启用，1停用") Integer status)
        implements Serializable {}
