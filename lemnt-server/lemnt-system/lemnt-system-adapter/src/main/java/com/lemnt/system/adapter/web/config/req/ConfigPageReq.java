package com.lemnt.system.adapter.web.config.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 参数配置分页查询条件对象
 *
 * @author wangxx
 */
@Schema(description = "参数配置分页查询条件")
public record ConfigPageReq(
        /**
 * 参数名称
 */
        @Schema(description = "参数名称")
        String configName,
        /**
 * 参数键名
 */
        @Schema(description = "参数键名")
        String configKey,
        /**
 * 系统内置（1是 0否）
 */
        @Schema(description = "系统内置（1是 0否）")
        String configType,
        /**
 * 开始时间
 */
        @Schema(description = "开始时间")
        LocalDateTime beginTime,
        /**
 * 结束时间
 */
        @Schema(description = "结束时间")
        LocalDateTime endTime,
        @Schema(description = "页码")
        Integer pageNum,
        @Schema(description = "每页数量")
        Integer pageSize
) implements Serializable {
    public ConfigPageReq {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
    }
}
