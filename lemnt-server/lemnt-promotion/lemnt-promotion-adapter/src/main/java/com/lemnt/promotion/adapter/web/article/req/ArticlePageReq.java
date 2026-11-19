package com.lemnt.promotion.adapter.web.article.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@Schema(description = "商城内容分页")
public record ArticlePageReq(
        @Schema(description = "名称包含筛选") @Size(max = 200) String title,
        @Schema(description = "分类编号") @Positive Long categoryId,
        @Schema(description = "热门推荐条件") Boolean recommendHot,
        @Schema(description = "轮播推荐条件") Boolean recommendBanner,
        @Schema(description = "0启用，1停用") @Min(0) @Max(1) Integer status,
        @Schema(description = "页码，默认1") @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页条数，默认20，最多100") @Min(1) @Max(100) Integer pageSize)
        implements Serializable {}
