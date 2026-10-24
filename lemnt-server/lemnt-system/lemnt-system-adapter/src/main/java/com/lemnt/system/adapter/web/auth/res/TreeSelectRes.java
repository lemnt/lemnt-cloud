package com.lemnt.system.adapter.web.auth.res;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 树选择信息响应
 *
 * @author wangxx
 */
@Schema(description = "树选择信息响应")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record TreeSelectRes(
        /**
         * 节点ID
         */
        @Schema(description = "节点ID")
        Long id,
        /**
         * 节点名称
         */
        @Schema(description = "节点名称")
        String label,
        /**
         * 节点禁用
         */
        @Schema(description = "节点禁用")
        boolean disabled,
        /**
         * 子节点
         */
        @Schema(description = "子节点")
        List<TreeSelectRes> children
) implements Serializable {}
