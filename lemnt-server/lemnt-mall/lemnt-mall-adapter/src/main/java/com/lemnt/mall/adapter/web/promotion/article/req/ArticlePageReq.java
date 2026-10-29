package com.lemnt.mall.adapter.web.promotion.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** 商城文章分页请求。 */
@Schema(description = "商城文章分页请求")
public record ArticlePageReq(
        @Schema(description = "文章分类编号") @Positive Long categoryId,
        @Schema(description = "关联商品编号，0表示未关联") @Min(0) Long spuId,
        @Schema(description = "文章标题") @Size(max = 255) String title,
        @Schema(description = "作者") @Size(max = 128) String author,
        @Schema(description = "状态：0启用1禁用") @Min(0) @Max(1) Integer status,
        @Schema(description = "是否热门") Boolean recommendHot,
        @Schema(description = "是否轮播") Boolean recommendBanner,
        @Schema(description = "创建起始时间（含）") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime createFrom,
        @Schema(description = "创建结束时间（含）") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime createTo,
        @Schema(description = "页码，默认1") @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页数量，默认20最多100") @Min(1) @Max(100) Integer pageSize
) {}
