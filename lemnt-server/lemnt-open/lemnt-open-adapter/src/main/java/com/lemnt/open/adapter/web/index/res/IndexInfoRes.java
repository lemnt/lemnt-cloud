package com.lemnt.open.adapter.web.index.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 首页信息响应。
 *
 * @author wangxx
 */
@Schema(name = "IndexInfoRes", description = "开放平台首页信息响应")
public record IndexInfoRes(
        @Schema(description = "平台名称", example = "test", requiredMode = Schema.RequiredMode.REQUIRED)
        String name
) implements Serializable {
}
