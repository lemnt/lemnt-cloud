package com.lemnt.system.adapter.web.config.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 修改字典数据请求
 *
 * @author wangxx
 */
@Schema(description = "修改字典数据请求")
public record DictDataUpdateReq(
        @NotNull(message = "字典编码不能为空")
        @Schema(description = "字典编码")
        Long dictCode,
        @Schema(description = "字典排序")
        Long dictSort,
        @NotBlank(message = "字典标签不能为空")
        @Schema(description = "字典标签")
        String dictLabel,
        @NotBlank(message = "字典键值不能为空")
        @Schema(description = "字典键值")
        String dictValue,
        @NotBlank(message = "字典类型不能为空")
        @Schema(description = "字典类型")
        String dictType,
        @Schema(description = "样式属性")
        String cssClass,
        @Schema(description = "表格字典样式")
        String listClass,
        @Schema(description = "是否默认（1是 0否）")
        @Pattern(regexp = "[01]", message = "是否默认值只能为1或0")
        String isDefault,
        @Schema(description = "状态（1正常 2停用）")
        @Pattern(regexp = "[12]", message = "状态值只能为1或2")
        String status,
        @Schema(description = "备注")
        String remark
) implements Serializable {}
