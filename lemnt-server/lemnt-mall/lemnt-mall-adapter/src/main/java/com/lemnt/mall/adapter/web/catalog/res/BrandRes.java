package com.lemnt.mall.adapter.web.catalog.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(description = "Brand响应")
public record BrandRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "名称") String name,
        @Schema(description = "图片URL") String picUrl,
        @Schema(description = "描述") String description,
        @Schema(description = "显示排序") Integer sort,
        @Schema(description = "状态") String status)
        implements Serializable {}
