package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/** 后台分类树节点。 */
@Schema(description = "后台分类树节点")
public record CategoryTreeRes(
        @Schema(description = "分类ID")
        Long id,
        @Schema(description = "分类名称")
        String name,
        @Schema(description = "分类图片")
        String picUrl,
        @Schema(description = "排序")
        Integer sort,
        @Schema(description = "分类启用状态")
        String status,
        @Schema(description = "子分类")
        List<CategoryTreeRes> children
) implements Serializable {
}
