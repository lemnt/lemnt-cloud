package com.lemnt.system.adapter.web.config.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 字典数据信息响应
 *
 * @author wangxx
 */
@Schema(description = "字典数据信息响应")
public record DictDataRes(
        /**
 * 字典编码
 */
        @Schema(description = "字典编码")
        Long dictCode,
        /**
 * 字典排序
 */
        @Schema(description = "字典排序")
        Long dictSort,
        /**
 * 字典标签
 */
        @Schema(description = "字典标签")
        String dictLabel,
        /**
 * 字典键值
 */
        @Schema(description = "字典键值")
        String dictValue,
        /**
 * 字典类型
 */
        @Schema(description = "字典类型")
        String dictType,
        /**
 * 样式属性
 */
        @Schema(description = "样式属性")
        String cssClass,
        /**
 * 表格字典样式
 */
        @Schema(description = "表格字典样式")
        String listClass,
        /**
 * 是否默认（1是 0否）
 */
        @Schema(description = "是否默认（1是 0否）")
        String isDefault,
        /**
 * 状态（1正常 2停用）
 */
        @Schema(description = "状态（1正常 2停用）")
        String status,
        /**
 * 备注
 */
        @Schema(description = "备注")
        String remark
) implements Serializable {}
