package com.lemnt.system.adapter.web.area.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/** 后台地区树独立响应。 */
@Schema(description = "地区树节点")
public record AreaTreeRes(
        @Schema(description = "编号") Integer id,
        @Schema(description = "地区名称") String name,
        @Schema(description = "下级地区") List<AreaTreeRes> children)
        implements Serializable {}
