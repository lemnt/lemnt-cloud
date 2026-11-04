package com.lemnt.system.adapter.web.auth.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 路由展示信息，字段保持原MetaDTO的HTTP形状。
 */
@Schema(description = "路由展示信息")
public record MetaRes(
    @Schema(description = "路由标题") String title,
    @Schema(description = "图标名称") String icon,
    @Schema(description = "是否禁止缓存") boolean noCache,
    @Schema(description = "内链地址") String link)
    implements Serializable {
}
