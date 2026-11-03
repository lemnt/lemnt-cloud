package com.lemnt.promotion.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

/** Banner 分页请求。 */
@Schema(description = "Banner 分页请求")
public record BannerPageReq(
        @Schema(description = "标题") String title,
        @Schema(description = "0 开启，1 关闭") Integer status,
        @Schema(description = "pageNum") Integer pageNum,
        @Schema(description = "pageSize") Integer pageSize)
        implements java.io.Serializable {}
