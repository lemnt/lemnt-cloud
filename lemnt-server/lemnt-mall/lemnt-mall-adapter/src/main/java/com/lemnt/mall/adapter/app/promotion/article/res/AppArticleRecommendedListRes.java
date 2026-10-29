package com.lemnt.mall.adapter.app.promotion.article.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** App文章推荐列表响应。 */
@Schema(description = "App文章推荐列表响应")
public record AppArticleRecommendedListRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "文章标题") String title,
        @Schema(description = "作者") String author,
        @Schema(description = "文章分类编号") Long categoryId,
        @Schema(description = "图片地址") String picUrl,
        @Schema(description = "简介") String introduction,
        @Schema(description = "文章内容") String content,
        @Schema(description = "创建时间") java.time.LocalDateTime createTime,
        @Schema(description = "实际浏览次数") Integer browseCount,
        @Schema(description = "关联商品编号，0表示未关联") Long spuId
) {}
