package com.lemnt.mall.adapter.web.catalog.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 分类响应。
 */
@Schema(description = "Category响应")
public record MallCategoryDetailRes(
        @Schema(description = "编号")
        Long id,
        @Schema(description = "父分类编号，根为null")
        Long parentId,
        @Schema(description = "名称")
        String name,
        @Schema(description = "图片URL")
        String picUrl,
        @Schema(description = "显示排序")
        Integer sort,
        @Schema(description = "状态")
        String status
) implements Serializable {}
