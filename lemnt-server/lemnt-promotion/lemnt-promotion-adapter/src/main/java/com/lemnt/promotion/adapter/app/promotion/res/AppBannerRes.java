package com.lemnt.promotion.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 小程序 Banner 响应。 */
@Schema(description = "小程序 Banner 响应")
public record AppBannerRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "标题") String title,
        @Schema(description = "跳转链接") String url,
        @Schema(description = "图片地址") String picUrl)
        implements java.io.Serializable {}
