package com.lemnt.mall.adapter.web.promotion.article.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 商城文章分页响应。 */
@Schema(description = "商城文章分页响应")
public record ArticlePageRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "文章分类编号") Long categoryId,
        @Schema(description = "关联商品编号，0表示未关联") Long spuId,
        @Schema(description = "文章标题") String title,
        @Schema(description = "作者") String author,
        @Schema(description = "图片地址") String picUrl,
        @Schema(description = "简介") String introduction,
        @Schema(description = "排序") Integer sort,
        @Schema(description = "状态：0启用1禁用") Integer status,
        @Schema(description = "是否热门") Boolean recommendHot,
        @Schema(description = "是否轮播") Boolean recommendBanner,
        @Schema(description = "文章内容") String content,
        @Schema(description = "实际浏览次数") Integer browseCount,
        @Schema(description = "创建时间") java.time.LocalDateTime createTime
) {}
