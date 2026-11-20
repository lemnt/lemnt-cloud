package com.lemnt.promotion.adapter.web.article.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@Schema(description = "商城内容管理请求")
public record ArticleSaveReq(
        @Schema(description = "分类编号") @NotNull @Positive Long categoryId,
        @Schema(description = "关联商品SPU编号") @Positive Long spuId,
        @Schema(description = "标题") @NotBlank @Size(max = 200) String title,
        @Schema(description = "作者") @Size(max = 100) String author,
        @Schema(description = "封面") @Size(max = 500) String picUrl,
        @Schema(description = "摘要") @Size(max = 500) String introduction,
        @Schema(description = "排序，值越大越靠前") @NotNull @Min(0) Integer sort,
        @Schema(description = "0启用，1停用") @NotNull @Min(0) @Max(1) Integer status,
        @Schema(description = "热门推荐") @NotNull Boolean recommendHot,
        @Schema(description = "轮播推荐") @NotNull Boolean recommendBanner,
        @Schema(description = "文章正文") @NotBlank @Size(max = 60000) String content)
        implements Serializable {}
