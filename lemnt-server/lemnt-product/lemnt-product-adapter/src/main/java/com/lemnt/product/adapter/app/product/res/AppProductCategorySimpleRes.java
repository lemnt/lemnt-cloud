package com.lemnt.product.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 分类响应。
 */
@Schema(description = "分类响应")
public record AppProductCategorySimpleRes(
    @Schema(description = "id") Long id,
    @Schema(description = "parentId") Long parentId,
    @Schema(description = "name") String name,
    @Schema(description = "picUrl") String picUrl)
    implements Serializable {
}
