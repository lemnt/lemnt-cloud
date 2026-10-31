package com.lemnt.mall.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 浏览足迹清空响应。 */
@Schema(description = "浏览足迹清空响应")
public record AppProductBrowseHistoryClearRes(
        @Schema(description = "是否清空成功") boolean success
) {}
