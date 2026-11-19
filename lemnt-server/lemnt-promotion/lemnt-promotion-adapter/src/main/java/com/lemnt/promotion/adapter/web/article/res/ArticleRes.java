package com.lemnt.promotion.adapter.web.article.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "商城内容查询响应")
public record ArticleRes(
        @Schema(description = "记录编号") Long id,
        @Schema(description = "分类编号") Long categoryId,
        @Schema(description = "关联商品SPU编号") Long spuId,
        @Schema(description = "标题") String title,
        @Schema(description = "作者") String author,
        @Schema(description = "封面") String picUrl,
        @Schema(description = "摘要") String introduction,
        @Schema(description = "排序，值越大越靠前") Integer sort,
        @Schema(description = "0启用，1停用") Integer status,
        @Schema(description = "热门推荐") Boolean recommendHot,
        @Schema(description = "轮播推荐") Boolean recommendBanner,
        @Schema(description = "文章正文") String content,
        @Schema(description = "累计浏览次数") Long browseCount)
        implements Serializable {}
