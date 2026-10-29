package com.lemnt.mall.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 浏览足迹添加响应。 */
@Schema(description = "浏览足迹添加响应")
public record AppProductBrowseHistoryCreateRes(
        @Schema(description = "是否记录成功") boolean success
) {}
