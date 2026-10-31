package com.lemnt.mall.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 浏览足迹移除响应。 */
@Schema(description = "浏览足迹移除响应")
public record AppProductBrowseHistoryDeleteRes(
        @Schema(description = "是否隐藏成功") boolean success
) {}
