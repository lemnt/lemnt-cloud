package com.lemnt.system.adapter.web.config.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 修改参数配置请求
 *
 * @author wangxx
 */
@Schema(description = "修改参数配置请求")
public record ConfigUpdateReq(
        @NotNull(message = "参数主键不能为空")
        @Schema(description = "参数主键")
        Long configId,
        @NotBlank(message = "参数名称不能为空")
        @Schema(description = "参数名称")
        String configName,
        @NotBlank(message = "参数键名不能为空")
        @Schema(description = "参数键名")
        String configKey,
        @NotBlank(message = "参数键值不能为空")
        @Schema(description = "参数键值")
        String configValue,
        @Schema(description = "系统内置（1是 0否）")
        @Pattern(regexp = "[01]", message = "系统内置值只能为1或0")
        String configType,
        @Schema(description = "备注")
        String remark
) implements Serializable {}
