package com.lemnt.promotion.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** Banner 响应。 */
@Schema(description = "Banner 响应")
public record BannerRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "标题") String title,
        @Schema(description = "跳转链接") String url,
        @Schema(description = "图片地址") String picUrl,
        @Schema(description = "排序") Integer sort,
        @Schema(description = "0 开启，1 关闭") Integer status,
        @Schema(description = "位置") Integer position,
        @Schema(description = "备注") String memo,
        @Schema(description = "点击次数") Integer browseCount)
        implements Serializable {}
