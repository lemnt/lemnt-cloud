package com.lemnt.system.adapter.web.config.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 参数配置信息响应
 *
 * @author wangxx
 */
@Schema(description = "参数配置信息响应")
public record ConfigRes(
        /**
 * 参数主键
 */
        @Schema(description = "参数主键")
        Long configId,
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
 * 参数键值
 */
        @Schema(description = "参数键值")
        String configValue,
        /**
 * 系统内置（1是 0否）
 */
        @Schema(description = "系统内置（1是 0否）")
        String configType,
        /**
 * 备注
 */
        @Schema(description = "备注")
        String remark,
        /**
 * 创建时间
 */
        @Schema(description = "创建时间")
        LocalDateTime createTime
) implements Serializable {}
