package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 品牌分页记录。 */
@Schema(description = "品牌分页记录")
public record BrandPageRes(
        @Schema(description = "品牌ID")
        Long id,
        @Schema(description = "品牌名称")
        String name,
        @Schema(description = "品牌图标")
        String picUrl,
        @Schema(description = "品牌说明")
        String description,
        @Schema(description = "排序")
        Integer sort,
        @Schema(description = "品牌启用状态")
        String status
) implements Serializable {
}
