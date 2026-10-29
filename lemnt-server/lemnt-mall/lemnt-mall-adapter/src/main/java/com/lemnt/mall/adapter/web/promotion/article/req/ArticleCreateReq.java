package com.lemnt.mall.adapter.web.promotion.article.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** 商城文章新增请求。 */
@Schema(description = "商城文章新增请求")
public record ArticleCreateReq(
        @Schema(description = "文章分类编号") @Positive @NotNull Long categoryId,
        @Schema(description = "关联商品编号，0表示未关联") @Min(0) @NotNull Long spuId,
        @Schema(description = "文章标题") @Size(max = 255) @NotBlank String title,
        @Schema(description = "作者") @Size(max = 128) String author,
        @Schema(description = "图片地址") @Size(max = 1024) @NotBlank String picUrl,
        @Schema(description = "简介") @Size(max = 2000) String introduction,
        @Schema(description = "排序") @Min(0) @NotNull Integer sort,
        @Schema(description = "状态：0启用1禁用") @Min(0) @Max(1) @NotNull Integer status,
        @Schema(description = "是否热门") @NotNull Boolean recommendHot,
        @Schema(description = "是否轮播") @NotNull Boolean recommendBanner,
        @Schema(description = "文章内容") @Size(max = 1000000) @NotBlank String content
) {}
