package com.lemnt.system.adapter.web.config.res;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 字典类型信息响应
 *
 * @author wangxx
 */
@Schema(description = "字典类型信息响应")
public record DictTypeRes(
        /**
         * 字典主键
         */
        @Schema(description = "字典主键")
        Long dictId,
        /**
         * 字典名称
         */
        @Schema(description = "字典名称")
        String dictName,
        /**
         * 字典类型
         */
        @Schema(description = "字典类型")
        String dictType,
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
