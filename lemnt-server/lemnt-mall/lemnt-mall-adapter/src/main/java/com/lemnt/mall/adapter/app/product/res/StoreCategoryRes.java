package com.lemnt.mall.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 门店分类响应。
 */
@Schema(description = "StoreCategory公开响应")
public record StoreCategoryRes(
        @Schema(description = "id")
        Long id,
        @Schema(description = "parentId")
        Long parentId,
        @Schema(description = "name")
        String name,
        @Schema(description = "picUrl")
        String picUrl
) implements Serializable {}
