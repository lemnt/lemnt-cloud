package com.lemnt.system.adapter.web.config.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 新增字典类型请求
 *
 * @author wangxx
 */
@Schema(description = "新增字典类型请求")
public record DictTypeAddReq(
        @NotBlank(message = "字典名称不能为空")
        @Schema(description = "字典名称")
        String dictName,
        @NotBlank(message = "字典类型不能为空")
        @Schema(description = "字典类型")
        String dictType,
        @Schema(description = "状态（1正常 2停用）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status,
        @Schema(description = "备注")
        String remark
) implements Serializable {}
